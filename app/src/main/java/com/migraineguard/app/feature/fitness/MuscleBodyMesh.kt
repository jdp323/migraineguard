package com.migraineguard.app.feature.fitness

import kotlin.math.*

data class BodyVertex(val x:Float,val y:Float,val z:Float)
data class BodyFace(val a:BodyVertex,val b:BodyVertex,val c:BodyVertex,val muscle:Muscle?)
data class BodyPoint(val x:Float,val y:Float,val z:Float)
data class ProjectedBodyFace(val a:BodyPoint,val b:BodyPoint,val c:BodyPoint,val muscle:Muscle?,val light:Float)

/** Original low-poly, neutral adult training figure, generated locally without external model assets. */
object MuscleBodyMesh {
    private fun center(a:BodyVertex,b:BodyVertex,c:BodyVertex)=BodyVertex((a.x+b.x+c.x)/3,(a.y+b.y+c.y)/3,(a.z+b.z+c.z)/3)
    val faces:List<BodyFace> by lazy {
        val output=mutableListOf<BodyFace>()
        fun ellipsoid(cx:Float,cy:Float,cz:Float,rx:Float,ry:Float,rz:Float,tilt:Float=0f,zone:(BodyVertex)->Muscle?={null}) {
            fun v(i:Int,j:Int):BodyVertex {
                val phi=PI*i/8;val theta=2*PI*j/16
                val x=(rx*sin(phi)*cos(theta)).toFloat();val y=(ry*cos(phi)).toFloat()
                return BodyVertex(cx+x*cos(tilt)+y*sin(tilt),cy-x*sin(tilt)+y*cos(tilt),cz+(rz*sin(phi)*sin(theta)).toFloat())
            }
            for(i in 0 until 8)for(j in 0 until 16) {
                val a=v(i,j);val b=v(i+1,j);val c=v(i+1,j+1);val d=v(i,j+1)
                if(i>0)output+=BodyFace(a,b,d,zone(center(a,b,d)))
                if(i<7)output+=BodyFace(b,c,d,zone(center(b,c,d)))
            }
        }
        val rings=listOf(floatArrayOf(.22f,.065f,.06f),floatArrayOf(.26f,.22f,.08f),floatArrayOf(.34f,.20f,.105f),floatArrayOf(.42f,.15f,.085f),floatArrayOf(.48f,.14f,.08f),floatArrayOf(.54f,.17f,.095f),floatArrayOf(.57f,.12f,.07f))
        fun torso(i:Int,j:Int):BodyVertex {val r=rings[i];val angle=2*PI*j/20;return BodyVertex((cos(angle)*r[1]).toFloat(),r[0],(sin(angle)*r[2]).toFloat())}
        fun torsoZone(p:BodyVertex):Muscle?=when {
            p.y<.265f -> null
            p.z<0 && p.y>.49f -> Muscle.GLUTES
            p.z<0 && p.y<.35f -> Muscle.UPPER_BACK
            p.z<0 -> Muscle.LATS
            p.y<.365f -> Muscle.CHEST
            abs(p.x)>.105f -> Muscle.OBLIQUES
            else -> Muscle.CORE
        }
        for(i in 0 until rings.lastIndex)for(j in 0 until 20){val a=torso(i,j);val b=torso(i+1,j);val c=torso(i+1,j+1);val d=torso(i,j+1);output+=BodyFace(a,b,d,torsoZone(center(a,b,d)));output+=BodyFace(b,c,d,torsoZone(center(b,c,d)))}
        ellipsoid(0f,.125f,0f,.075f,.10f,.075f)
        ellipsoid(0f,.225f,0f,.058f,.05f,.052f)
        for(side in listOf(-1,1)) {
            val s=side.toFloat()
            ellipsoid(s*.23f,.29f,0f,.075f,.075f,.075f,tilt=s*-.35f,zone={Muscle.SHOULDERS})
            ellipsoid(s*.285f,.385f,0f,.052f,.10f,.055f,tilt=s*.25f,zone={if(it.z>=0)Muscle.BICEPS else Muscle.TRICEPS})
            ellipsoid(s*.325f,.535f,.005f,.038f,.10f,.04f,tilt=s*.12f)
            ellipsoid(s*.34f,.645f,.012f,.038f,.047f,.026f)
            ellipsoid(s*.095f,.65f,0f,.075f,.14f,.075f,tilt=s*-.08f,zone={if(it.z>=0)Muscle.QUADS else Muscle.HAMSTRINGS})
            ellipsoid(s*.115f,.79f,.005f,.048f,.04f,.05f)
            ellipsoid(s*.12f,.89f,-.005f,.045f,.105f,.052f,zone={Muscle.CALVES})
            ellipsoid(s*.12f,1.005f,.033f,.045f,.028f,.085f)
        }
        output
    }
    private data class IndexedFace(val a:Int,val b:Int,val c:Int,val muscle:Muscle?,val nx:Float,val ny:Float,val nz:Float)
    private val geometry by lazy {
        val indices=linkedMapOf<BodyVertex,Int>()
        fun index(v:BodyVertex)=indices.getOrPut(v){indices.size}
        val indexed=faces.map { f ->
            val ax=f.b.x-f.a.x;val ay=f.b.y-f.a.y;val az=f.b.z-f.a.z;val bx=f.c.x-f.a.x;val by=f.c.y-f.a.y;val bz=f.c.z-f.a.z
            val nx=ay*bz-az*by;val ny=az*bx-ax*bz;val nz=ax*by-ay*bx;val length=sqrt(nx*nx+ny*ny+nz*nz).coerceAtLeast(.000001f)
            IndexedFace(index(f.a),index(f.b),index(f.c),f.muscle,nx/length,ny/length,nz/length)
        }
        indices.keys.toList() to indexed
    }
    fun project(angleDegrees:Float,width:Float,height:Float):List<ProjectedBodyFace> {
        val angle=angleDegrees*PI.toFloat()/180f;val cs=cos(angle);val sn=sin(angle)
        val scale=minOf(height*.90f,width/1.05f)
        fun project(v:BodyVertex):BodyPoint {val x=v.x*cs+v.z*sn;val z=-v.x*sn+v.z*cs;val perspective=3f/(3f-z);return BodyPoint(width/2+x*scale*perspective,height/2+(v.y-.53f)*scale*perspective,z)}
        val points=geometry.first.map(::project)
        return geometry.second.map { f ->
            val light=(.45f+.55f*abs((f.nx*cs+f.nz*sn)*-.35f+f.ny*-.45f+(-f.nx*sn+f.nz*cs))).coerceIn(.4f,1f)
            ProjectedBodyFace(points[f.a],points[f.b],points[f.c],f.muscle,light)
        }.sortedBy { (it.a.z+it.b.z+it.c.z)/3 }
    }
    fun hit(angle:Float,width:Float,height:Float,x:Float,y:Float):Muscle? {
        fun side(p:BodyPoint,a:BodyPoint,b:BodyPoint)=(p.x-b.x)*(a.y-b.y)-(a.x-b.x)*(p.y-b.y)
        val p=BodyPoint(x,y,0f)
        for(f in project(angle,width,height).asReversed()) {
            val area=abs((f.b.x-f.a.x)*(f.c.y-f.a.y)-(f.b.y-f.a.y)*(f.c.x-f.a.x));if(area<.1f)continue
            val d1=side(p,f.a,f.b);val d2=side(p,f.b,f.c);val d3=side(p,f.c,f.a)
            if(!((d1<0||d2<0||d3<0)&&(d1>0||d2>0||d3>0)))return f.muscle
        }
        return null
    }
}

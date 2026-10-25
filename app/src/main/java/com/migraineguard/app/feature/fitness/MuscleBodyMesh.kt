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
        fun ellipsoid(cx:Float,cy:Float,cz:Float,rx:Float,ry:Float,rz:Float,tilt:Float=0f,bands:Int=6,sides:Int=16,zone:(BodyVertex)->Muscle?={null}) {
            fun v(i:Int,j:Int):BodyVertex {
                val phi=PI*i/bands;val theta=2*PI*j/sides
                val x=(rx*sin(phi)*cos(theta)).toFloat();val y=(ry*cos(phi)).toFloat()
                return BodyVertex(cx+x*cos(tilt)+y*sin(tilt),cy-x*sin(tilt)+y*cos(tilt),cz+(rz*sin(phi)*sin(theta)).toFloat())
            }
            for(i in 0 until bands)for(j in 0 until sides) {
                val a=v(i,j);val b=v(i+1,j);val c=v(i+1,j+1);val d=v(i,j+1)
                if(i>0)output+=BodyFace(a,b,d,zone(center(a,b,d)))
                if(i<bands-1)output+=BodyFace(b,c,d,zone(center(b,c,d)))
            }
        }
        val rings=listOf(floatArrayOf(.22f,.06f,.055f),floatArrayOf(.26f,.195f,.078f),floatArrayOf(.29f,.20f,.095f),floatArrayOf(.34f,.185f,.10f),floatArrayOf(.38f,.155f,.09f),floatArrayOf(.43f,.14f,.083f),floatArrayOf(.48f,.14f,.08f),floatArrayOf(.52f,.165f,.09f),floatArrayOf(.56f,.17f,.095f),floatArrayOf(.59f,.135f,.065f))
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
        ellipsoid(0f,.12f,0f,.067f,.095f,.07f,bands=10,sides=24)
        ellipsoid(0f,.217f,0f,.048f,.045f,.046f)
        ellipsoid(0f,.13f,.068f,.014f,.023f,.018f,bands=4,sides=12)
        for(side in listOf(-1,1)) {
            val sign=side.toFloat()
            ellipsoid(sign*.067f,.13f,0f,.012f,.024f,.017f,bands=4,sides=12)
            // Raised pectoral, abdominal and glute surfaces add readable anatomical contours.
            ellipsoid(sign*.079f,.322f,.078f,.074f,.052f,.035f,zone={Muscle.CHEST})
            for(y in listOf(.398f,.442f,.482f)) ellipsoid(sign*.036f,y,.082f,.032f,.022f,.017f,bands=4,sides=12,zone={Muscle.CORE})
            ellipsoid(sign*.076f,.548f,-.073f,.08f,.067f,.034f,zone={Muscle.GLUTES})
            fun limb(rings:List<FloatArray>,zone:(BodyVertex)->Muscle?) {
                fun vertex(i:Int,j:Int):BodyVertex { val r=rings[i];val t=2*PI*j/20;return BodyVertex(sign*(r[0]+(cos(t)*r[2]).toFloat()),r[1],(sin(t)*r[3]).toFloat()) }
                for(i in 0 until rings.lastIndex)for(j in 0 until 20){val a=vertex(i,j);val b=vertex(i+1,j);val c=vertex(i+1,j+1);val d=vertex(i,j+1);output+=BodyFace(a,b,d,zone(center(a,b,d)));output+=BodyFace(b,c,d,zone(center(b,c,d)))}
            }
            limb(listOf(floatArrayOf(.205f,.265f,.059f,.06f),floatArrayOf(.25f,.31f,.057f,.058f),floatArrayOf(.28f,.355f,.048f,.052f),floatArrayOf(.295f,.40f,.043f,.049f),floatArrayOf(.312f,.45f,.03f,.032f),floatArrayOf(.327f,.50f,.035f,.038f),floatArrayOf(.335f,.555f,.027f,.03f),floatArrayOf(.338f,.61f,.019f,.022f))) { p -> when { p.y<.325f -> Muscle.SHOULDERS; p.y<.445f -> if(p.z>=0)Muscle.BICEPS else Muscle.TRICEPS;else -> null } }
            ellipsoid(sign*.34f,.645f,.007f,.027f,.043f,.021f,sides=12)
            limb(listOf(floatArrayOf(.09f,.54f,.071f,.075f),floatArrayOf(.095f,.59f,.078f,.082f),floatArrayOf(.105f,.65f,.068f,.072f),floatArrayOf(.113f,.72f,.05f,.058f),floatArrayOf(.12f,.78f,.036f,.044f),floatArrayOf(.122f,.825f,.042f,.048f),floatArrayOf(.124f,.88f,.044f,.055f),floatArrayOf(.127f,.94f,.029f,.034f),floatArrayOf(.13f,.99f,.021f,.024f))) { p -> if(p.y<.79f) { if(p.z>=0)Muscle.QUADS else Muscle.HAMSTRINGS } else Muscle.CALVES }
            ellipsoid(sign*.13f,1.012f,.035f,.035f,.025f,.08f,sides=12)
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

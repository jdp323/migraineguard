import unittest
from check_android_crashes import classify

class CrashGateTest(unittest.TestCase):
    def block(self, process):
        return "10-31 E AndroidRuntime: FATAL EXCEPTION: main\n10-31 E AndroidRuntime: Process: " + process + ", PID: 42\n"
    def test_empty(self):
        self.assertEqual((False, []), classify("--------- beginning of crash\n"))
    def test_app(self):
        self.assertTrue(classify(self.block("com.migraineguard.app"))[0])
    def test_background(self):
        self.assertTrue(classify(self.block("com.migraineguard.app:worker"))[0])
    def test_instrumentation(self):
        self.assertTrue(classify(self.block("com.migraineguard.app.test"))[0])
    def test_wallpaper(self):
        self.assertEqual((False, ["com.google.android.apps.wallpaper"]), classify(self.block("com.google.android.apps.wallpaper")))
    def test_mixed(self):
        self.assertTrue(classify(self.block("com.google.android.apps.wallpaper") + self.block("com.migraineguard.app"))[0])
    def test_unidentified(self):
        self.assertTrue(classify(self.block("com.google.android.apps.wallpaper") + "10-31 E AndroidRuntime: FATAL EXCEPTION: main\ntruncated\n")[0])
    def test_boundary(self):
        self.assertFalse(classify(self.block("com.migraineguard.application"))[0])

if __name__ == "__main__":
    unittest.main()

package net.opendasharchive.openarchive

import net.opendasharchive.openarchive.services.webdav.WebDavFragment
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class UnitTests {

    @Test
    fun emptyString() {
        Assert.assertEquals(WebDavFragment.REMOTE_PHP_ADDRESS, "/remote.php/webdav/")
    }
}
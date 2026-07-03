package dev.salemlift.data.db

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EscapeLikeQueryTest {
    @Test
    fun `wildcards and the escape character are escaped literally`() {
        assertEquals("""100\% Row""", escapeLikeQuery("100% Row"))
        assertEquals("""curl\_bar""", escapeLikeQuery("curl_bar"))
        assertEquals("""back\\slash""", escapeLikeQuery("""back\slash"""))
        assertEquals("plain bench", escapeLikeQuery("plain bench"))
    }
}

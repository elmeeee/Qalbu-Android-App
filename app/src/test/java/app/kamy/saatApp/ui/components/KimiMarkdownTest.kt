package app.kamy.saatApp.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KimiMarkdownTest {

    @Test
    fun parseMarkdownBlocks_separatesArabicLatinAndTranslation() {
        val markdown = """
            اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا
            *Allahumma inni as-aluka 'ilman naafi'an*
            Artinya: Ya Allah, sesungguhnya aku memohon kepada-Mu ilmu yang bermanfaat.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MdBlock.Paragraph)
        assertTrue((blocks[0] as MdBlock.Paragraph).isArabic)
        assertEquals("اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا", (blocks[0] as MdBlock.Paragraph).text)

        assertTrue(blocks[1] is MdBlock.Paragraph)
        assertEquals("*Allahumma inni as-aluka 'ilman naafi'an*", (blocks[1] as MdBlock.Paragraph).text)

        assertTrue(blocks[2] is MdBlock.Paragraph)
        assertEquals("Artinya: Ya Allah, sesungguhnya aku memohon kepada-Mu ilmu yang bermanfaat.", (blocks[2] as MdBlock.Paragraph).text)
    }

    @Test
    fun parseMarkdownBlocks_handlesExplicitBlankLines() {
        val markdown = """
            رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً

            *Rabbana aatina fid-dunya hasanah*

            Artinya: "Wahai Tuhan kami, berikanlah kami kebaikan di dunia."
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MdBlock.Paragraph && (blocks[0] as MdBlock.Paragraph).isArabic)
        assertTrue(blocks[1] is MdBlock.Paragraph && !(blocks[1] as MdBlock.Paragraph).isArabic)
        assertTrue(blocks[2] is MdBlock.Paragraph && !(blocks[2] as MdBlock.Paragraph).isArabic)
    }
}


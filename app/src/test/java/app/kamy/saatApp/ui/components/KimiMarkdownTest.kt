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
            Translation: O Allah, I ask You for beneficial knowledge.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MdBlock.Paragraph)
        assertTrue((blocks[0] as MdBlock.Paragraph).isArabic)
        assertEquals("اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا", (blocks[0] as MdBlock.Paragraph).text)

        assertTrue(blocks[1] is MdBlock.Paragraph)
        assertEquals("*Allahumma inni as-aluka 'ilman naafi'an*", (blocks[1] as MdBlock.Paragraph).text)

        assertTrue(blocks[2] is MdBlock.Paragraph)
        assertEquals("Translation: O Allah, I ask You for beneficial knowledge.", (blocks[2] as MdBlock.Paragraph).text)
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

    @Test
    fun parseMarkdownBlocks_groupsBlockquotesWithoutEmptyBoxes() {
        val markdown = """
            > Qur'an 2:187 - The verse lists what nullifies the fast.
            >
            > Arabic:
            >
            > إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ
            >
            > idhā jā'a naṣru llāhi wa al-fathu
            >
            > Translation: When the victory of Allah has come.
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)

        // Must be exactly 1 grouped quote block, NOT multiple fragmented boxes!
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MdBlock.Quote)
        val quote = blocks[0] as MdBlock.Quote
        // Inner blocks should contain the non-empty children
        assertTrue(quote.children.isNotEmpty())
        // None of the children should be an empty paragraph
        assertTrue(quote.children.none { it is MdBlock.Paragraph && it.text.isBlank() })
    }
}

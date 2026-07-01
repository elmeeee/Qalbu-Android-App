package app.kamy.saatApp.features.quran

import androidx.annotation.StringRes
import app.kamy.saatApp.R

data class IqraLesson(
    val id: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val contentRes: Int,
    @StringRes val detailRes: Int
)

object IqraLessons {
    val all = listOf(
        IqraLesson(
            id = 1,
            titleRes = R.string.iqra_lesson_1_title,
            descriptionRes = R.string.iqra_lesson_1_desc,
            contentRes = R.string.iqra_lesson_1_content,
            detailRes = R.string.iqra_lesson_1_detail
        ),
        IqraLesson(
            id = 2,
            titleRes = R.string.iqra_lesson_2_title,
            descriptionRes = R.string.iqra_lesson_2_desc,
            contentRes = R.string.iqra_lesson_2_content,
            detailRes = R.string.iqra_lesson_2_detail
        ),
        IqraLesson(
            id = 3,
            titleRes = R.string.iqra_lesson_3_title,
            descriptionRes = R.string.iqra_lesson_3_desc,
            contentRes = R.string.iqra_lesson_3_content,
            detailRes = R.string.iqra_lesson_3_detail
        ),
        IqraLesson(
            id = 4,
            titleRes = R.string.iqra_lesson_4_title,
            descriptionRes = R.string.iqra_lesson_4_desc,
            contentRes = R.string.iqra_lesson_4_content,
            detailRes = R.string.iqra_lesson_4_detail
        ),
        IqraLesson(
            id = 5,
            titleRes = R.string.iqra_lesson_5_title,
            descriptionRes = R.string.iqra_lesson_5_desc,
            contentRes = R.string.iqra_lesson_5_content,
            detailRes = R.string.iqra_lesson_5_detail
        ),
        IqraLesson(
            id = 6,
            titleRes = R.string.iqra_lesson_6_title,
            descriptionRes = R.string.iqra_lesson_6_desc,
            contentRes = R.string.iqra_lesson_6_content,
            detailRes = R.string.iqra_lesson_6_detail
        )
    )
}

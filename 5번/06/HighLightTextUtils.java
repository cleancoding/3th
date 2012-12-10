package jp.naver.cafe.android.util;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

/**
 */
public class HighLightTextUtils {

	public static SpannableStringBuilder getBackgroundHighLight(String highLightTags, String highLightText,
			int backgroundColor) {

		String startTag = highLightTags.substring(0, highLightTags.indexOf("."));
		String endTag = highLightTags.substring(highLightTags.indexOf(".") + 1, highLightTags.length());
		SpannableStringBuilder spb = new SpannableStringBuilder(highLightText);

		while (true) {
			int startIdx = highLightText.lastIndexOf(startTag);
			int endIdx = highLightText.lastIndexOf(endTag);
			if (startIdx != -1 && endIdx != -1) {
				spb.setSpan(new BackgroundColorSpan(backgroundColor), startIdx + startTag.length(), endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				spb.replace(endIdx, endIdx + endTag.length(), "");
				spb.replace(startIdx, startIdx + startTag.length(), "");
				highLightText = spb.toString();
			} else {
				break;
			}
		}
		return spb;
	}

	public static SpannableStringBuilder getStringHighLight(String highLightTags, String highLightText,
			int stringColor) {

		String startTag = highLightTags.substring(0, highLightTags.indexOf("."));
		String endTag = highLightTags.substring(highLightTags.indexOf(".") + 1, highLightTags.length());
		SpannableStringBuilder spb = new SpannableStringBuilder(highLightText);

		while (true) {
			int startIdx = highLightText.lastIndexOf(startTag);
			int endIdx = highLightText.lastIndexOf(endTag);
			if (startIdx != -1 && endIdx != -1) {
				spb.setSpan(new ForegroundColorSpan(stringColor), startIdx + startTag.length(), endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				spb.replace(endIdx, endIdx + endTag.length(), "");
				spb.replace(startIdx, startIdx + startTag.length(), "");
				highLightText = spb.toString();
			} else {
				break;
			}
		}
		return spb;
	}

	/**
	 * HightlightTag가 없는 String 전체에 컬러를 입히는 메소드.
	 * @param text - 대상 텍스트
	 * @param color - 텍스트 전체에 입힐 컬러(ex. 0x01234567)
	 * @return 컬러가 입혀진 SpannableString
	 */
	public static SpannableStringBuilder getStringHighLightNonTag(String text, int color) {
		SpannableStringBuilder spb = new SpannableStringBuilder(text);
		spb.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spb;
	}

	/**
	 * 전체 문장에서 넘어오는 단어를 찾아 제일 처음 위치한 곳에만 컬러를 입히는 메소드
	 * @param sentence - 전체 문장
	 * @param word - 대상 단어
	 * @param color - 텍스트 전체에 입힐 컬러(ex. 0x01234567)
	 * @return 컬러가 입혀진 SpannableString
	 */
	public static SpannableStringBuilder getStrinHighLightFirstWord(String sentence, String word, int color) {
		SpannableStringBuilder spb = new SpannableStringBuilder(sentence);
		int startIdx = sentence.indexOf(word);
		if (startIdx != -1) {
			spb.setSpan(new ForegroundColorSpan(color), startIdx, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spb;
	}

}

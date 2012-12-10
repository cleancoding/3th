    prevButton.setEnabled(enableFlag);
        prevButton.setAlpha((enableFlag /*== true*/) ? 0xff : 0x33);
        if (enableFlag /*== true*/) {
            prevButton.setImageResource(R.drawable.selector_toolbar_prev_message);
            prevButton.setBackgroundResource(R.drawable.selector_default_image_button_bkgrnd);
        }

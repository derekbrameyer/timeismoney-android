package com.doomonafireball.timeismoney.android.activity;

import com.google.inject.Inject;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.doomonafireball.betterpickers.hmspicker.HmsPickerBuilder;
import com.doomonafireball.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.doomonafireball.timeismoney.android.Datastore;
import com.doomonafireball.timeismoney.android.R;
import com.doomonafireball.timeismoney.android.fragment.AboutFragment;
import com.doomonafireball.timeismoney.android.util.FontTypefaceSpan;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

public class MainActivity extends RoboSherlockFragmentActivity implements
        NumberPickerDialogFragment.NumberPickerDialogHandler, HmsPickerDialogFragment.HmsPickerDialogHandler {

    @InjectView(R.id.hourly_rate) TextView hourlyRate;
    @InjectView(R.id.people_count) TextView peopleCount;
    @InjectView(R.id.plus) ImageView plus;
    @InjectView(R.id.minus) ImageView minus;
    @InjectView(R.id.go_stop) TextView goStop;
    @InjectView(R.id.cost) TextView cost;
    @InjectView(R.id.time_elapsed) TextView timeElapsed;
    @InjectView(R.id.reset) ImageView reset;

    @InjectResource(R.string.people) String peopleString;

    @Inject Datastore mDatastore;

    private BigDecimal mHourlyRate = new BigDecimal(20);
    private BigDecimal mMillisRate = new BigDecimal(20d / 60d / 60d / 1000d);
    private BigDecimal mPeopleCount = new BigDecimal(1);
    private BigDecimal mCost = new BigDecimal(0);
    private long mCurrentTimeElapsed = 0l;
    private long mTotalTimeElapsed = 0l;
    private long mStartTime = 0l;

    private Handler mHandler;
    private boolean pickingHourlyRate = true;
    private String mCurrencySymbol;
    private NumberPickerBuilder mHourlyRatePickerBuilder;
    private NumberPickerBuilder mPeopleCountPickerBuilder;
    private HmsPickerBuilder mHmsPickerBuilder;
    private ShareActionProvider mShareActionProvider;

    private int mCostTextSizeOnes;
    private int mCostTextSizeTens;
    private int mCostTextSizeHundreds;
    private int mCostTextSizeThousands;
    private int mCostTextSizeTenThousands;
    private int mCostTextSizeHundredThousands;
    private int mCostTextSizeMillions;
    private int mCostTextSizeTenMillions;
    private int mCostTextSizeHundredMillions;

    private static final BigDecimal oneHundred = new BigDecimal(100);
    private static final BigDecimal oneThousand = new BigDecimal(1000);
    private static final BigDecimal tenThousand = new BigDecimal(10000);
    private static final BigDecimal hundredThousand = new BigDecimal(100000);
    private static final BigDecimal million = new BigDecimal(1000000);
    private static final BigDecimal tenMillion = new BigDecimal(10000000);
    private static final BigDecimal hundredMillion = new BigDecimal(100000000);

    private class ConfigurationObject {

        public BigDecimal hourlyRate;
        public BigDecimal millisRate;
        public BigDecimal peopleCount;
        public BigDecimal cost;
        public long currentTimeElapsed;
        public long totalTimeElapsed;
        public long startTime;
        public boolean goStopIsSelected;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        getSupportActionBar().setHomeButtonEnabled(true);

        SpannableString s = new SpannableString(getString(R.string.app_name));
        s.setSpan(new FontTypefaceSpan(this, getString(R.string.default_font)), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        mHandler = new Handler();
        final ConfigurationObject config = (ConfigurationObject) getLastCustomNonConfigurationInstance();
        if (config != null) {
            mHourlyRate = config.hourlyRate;
            mMillisRate = config.millisRate;
            mPeopleCount = config.peopleCount;
            mCost = config.cost;
            mCurrentTimeElapsed = config.currentTimeElapsed;
            mTotalTimeElapsed = config.totalTimeElapsed;
            mStartTime = config.startTime;
            goStop.setSelected(config.goStopIsSelected);
            if (config.goStopIsSelected) {
                // Start the timer up
                goStop.setText(R.string.stop_excl);
                reset.setVisibility(View.GONE);
                mHandler.post(updater);
            } else {
                goStop.setText(R.string.go_excl);
                mHandler.removeCallbacks(updater);
                if (mCost.compareTo(BigDecimal.ZERO) > 0) {
                    reset.setVisibility(View.VISIBLE);
                    setShareIntent();
                }
            }
        } else {
            mHourlyRate = mDatastore.getHourlyRate();
            mMillisRate = mHourlyRate.divide(new BigDecimal(60d * 60d * 1000d), 20, BigDecimal.ROUND_HALF_UP);
            mPeopleCount = mDatastore.getPeopleCount();
        }

        mCostTextSizeOnes = getResources().getDimensionPixelSize(R.dimen.cost_text_size_ones);
        mCostTextSizeTens = getResources().getDimensionPixelSize(R.dimen.cost_text_size_tens);
        mCostTextSizeHundreds = getResources().getDimensionPixelSize(R.dimen.cost_text_size_hundreds);
        mCostTextSizeThousands = getResources().getDimensionPixelSize(R.dimen.cost_text_size_thousands);
        mCostTextSizeTenThousands = getResources().getDimensionPixelSize(R.dimen.cost_text_size_ten_thousands);
        mCostTextSizeHundredThousands = getResources().getDimensionPixelSize(R.dimen.cost_text_size_hundred_thousands);
        mCostTextSizeMillions = getResources().getDimensionPixelSize(R.dimen.cost_text_size_millions);
        mCostTextSizeTenMillions = getResources().getDimensionPixelSize(R.dimen.cost_text_size_ten_millions);
        mCostTextSizeHundredMillions = getResources().getDimensionPixelSize(R.dimen.cost_text_size_hundred_millions);

        Currency curr = Currency.getInstance(Locale.getDefault());
        mCurrencySymbol = curr.getSymbol();
        String currencyCode = curr.getCurrencyCode();

        setHourlyRateText();
        setPeopleCountText();
        setCost();
        setTimeElapsed();

        FragmentManager fm = getSupportFragmentManager();
        mHourlyRatePickerBuilder = new NumberPickerBuilder()
                .setFragmentManager(fm)
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .setLabelText(String.format(getString(R.string.currency_per_hour), currencyCode))
                .setPlusMinusVisibility(View.GONE)
                .setDecimalVisibility(View.GONE);
        mPeopleCountPickerBuilder = new NumberPickerBuilder()
                .setFragmentManager(fm)
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .setLabelText(peopleString)
                .setPlusMinusVisibility(View.GONE)
                .setDecimalVisibility(View.GONE);
        mHmsPickerBuilder = new HmsPickerBuilder()
                .setFragmentManager(fm)
                .setStyleResId(R.style.BetterPickersDialogFragment_Light);

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                mPeopleCount = mPeopleCount.add(BigDecimal.ONE);
                mDatastore.persistPeopleCount(mPeopleCount);
                setPeopleCountText();
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (mPeopleCount.intValue() > 1) {
                    mPeopleCount = mPeopleCount.subtract(BigDecimal.ONE);
                    mDatastore.persistPeopleCount(mPeopleCount);
                    setPeopleCountText();
                }
            }
        });
        hourlyRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                pickingHourlyRate = true;
                mHourlyRatePickerBuilder.show();
            }
        });
        peopleCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                pickingHourlyRate = false;
                mPeopleCountPickerBuilder.show();
            }
        });
        timeElapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                mHmsPickerBuilder.show();
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeOnes);
                mCost = new BigDecimal(0);
                mTotalTimeElapsed = 0l;
                cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeOnes);
                setCost();
                setTimeElapsed();
                reset.setVisibility(View.GONE);
            }
        });
        goStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (goStop.isSelected()) {
                    // Stop!
                    goStop.setText(R.string.go_excl);
                    reset.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(updater);
                    setShareIntent();
                } else {
                    // Go!
                    goStop.setText(R.string.stop_excl);
                    reset.setVisibility(View.GONE);
                    mStartTime = System.currentTimeMillis();
                    mCurrentTimeElapsed = 0l;
                    mHandler.post(updater);
                }
                goStop.setSelected(!goStop.isSelected());
            }
        });
    }

    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            BigDecimal difference = new BigDecimal(now - mStartTime - mCurrentTimeElapsed);
            mTotalTimeElapsed += (now - mStartTime - mCurrentTimeElapsed);
            BigDecimal costChange = mMillisRate.multiply(mPeopleCount).multiply(difference);
            mCost = mCost.add(costChange);
            setCost();
            mCurrentTimeElapsed = now - mStartTime;
            setTimeElapsed();
            mHandler.postDelayed(updater, 30);
        }
    };

    private void setCost() {
        cost.setText(NumberFormat.getCurrencyInstance().format(mCost));
        if (mCost.compareTo(hundredMillion) > 0) {
            cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeHundredMillions);
        } else if (mCost.compareTo(tenMillion) > 0) {
            cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeTenMillions);
        } else if (mCost.compareTo(million) > 0) {
            cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeMillions);
        } else if (mCost.compareTo(hundredThousand) > 0) {
            cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeHundredThousands);
        } else if (mCost.compareTo(tenThousand) > 0) {
            cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeTenThousands);
        } else if (mCost.compareTo(oneThousand) > 0) {
            cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeThousands);
        } else if (mCost.compareTo(oneHundred) > 0) {
            cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeHundreds);
        } else if (mCost.compareTo(BigDecimal.TEN) > 0) {
            cost.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCostTextSizeTens);
        }
    }

    private void setTimeElapsed() {
        timeElapsed.setText(getTimeElapsedText(mTotalTimeElapsed));
    }

    public static String getTimeElapsedText(long millis) {
        String text = String.format("%dms", millis);
        if (millis > 60l * 60l * 1000l) {
            // h m s ms
            int hours = (int) (millis / (1000 * 60 * 60));
            int remainder = (int) (millis % (1000 * 60 * 60));
            int minutes = remainder / (1000 * 60);
            remainder = remainder % (1000 * 60);
            int seconds = remainder / 1000;
            remainder = remainder % 1000;
            text = String.format("%dh %02dm %02ds %03dms", hours, minutes, seconds, remainder);
        } else if (millis > 1l * 60l * 1000l) {
            // m s ms
            int minutes = (int) (millis / (1000 * 60));
            int remainder = (int) (millis % (1000 * 60));
            int seconds = remainder / 1000;
            remainder = remainder % 1000;
            text = String.format("%dm %02ds %03dms", minutes, seconds, remainder);
        } else if (millis > 1l * 1l * 1000l) {
            // s ms
            int seconds = (int) (millis / (1000));
            int remainder = (int) (millis % (1000));
            text = String.format("%ds %03dms", seconds, remainder);
        }
        return text;
    }

    private void setHourlyRateText() {
        hourlyRate.setText(mCurrencySymbol + mHourlyRate.intValue());
    }

    private void setPeopleCountText() {
        peopleCount.setText(Integer.toString(mPeopleCount.intValue()));
    }

    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_default_text));
            if (mCurrentTimeElapsed > 0l && !mCost.equals(BigDecimal.ZERO)) {
                String shareExtra = String.format(getString(R.string.share_custom_text_zero), cost.getText().toString(),
                        timeElapsed.getText().toString());
                if ((mPeopleCount.intValue() - 1) > 0) {
                    shareExtra = getResources()
                            .getQuantityString(R.plurals.share_custom_text, mPeopleCount.intValue() - 1,
                                    cost.getText().toString(), timeElapsed.getText().toString(),
                                    mPeopleCount.intValue() - 1);
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareExtra);
            }
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        final ConfigurationObject config = new ConfigurationObject();
        config.hourlyRate = mHourlyRate;
        config.millisRate = mMillisRate;
        config.peopleCount = mPeopleCount;
        config.cost = mCost;
        config.currentTimeElapsed = mCurrentTimeElapsed;
        config.totalTimeElapsed = mTotalTimeElapsed;
        config.startTime = mStartTime;
        config.goStopIsSelected = goStop.isSelected();
        return config;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (goStop.isSelected()) {
            mHandler.removeCallbacks(updater);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (goStop.isSelected()) {
            mHandler.post(updater);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_activity_menu, menu);

        MenuItem actionItem = menu.findItem(R.id.menu_share_action_provider);
        mShareActionProvider = (ShareActionProvider) actionItem.getActionProvider();
        mShareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        setShareIntent();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AboutFragment aboutDialog = new AboutFragment();
                aboutDialog.show(getSupportFragmentManager(), "fragment_about");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogNumberSet(int number, double decimal, boolean isNegative, double fullNumber) {
        if (pickingHourlyRate) {
            mHourlyRate = new BigDecimal(number);
            mDatastore.persistHourlyRate(mHourlyRate);
            mMillisRate = mHourlyRate.divide(new BigDecimal(60d * 60d * 1000d), 20, BigDecimal.ROUND_HALF_UP);
            setHourlyRateText();
        } else {
            if (number > 0) {
                mPeopleCount = new BigDecimal(number);
                mDatastore.persistPeopleCount(mPeopleCount);
                setPeopleCountText();
            }
        }
    }

    @Override
    public void onDialogHmsSet(int hours, int minutes, int seconds) {
        if (mTotalTimeElapsed == 0l) {
            reset.setVisibility(View.VISIBLE);
        }
        mTotalTimeElapsed +=
                (((long) hours) * 60l * 60l * 1000l) + (((long) minutes) * 60l * 1000l) + (((long) seconds) * 1000l);
        mCost = mMillisRate.multiply(mPeopleCount).multiply(new BigDecimal(mTotalTimeElapsed));
        setCost();
        setTimeElapsed();
    }
}


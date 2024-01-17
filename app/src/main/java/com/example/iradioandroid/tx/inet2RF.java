package com.example.iradioandroid.tx;

public class inet2RF {
    private static final String TAG = "inet2RF";

    private static int IF_OFFSET = 0; // +/- intermediate frequency offset in kHz, normally 455, 450, ...

    private static int MINIMAL_TX_FRQ = 526;

    private static int MAXIMAL_TX_FRQ = 1606;

    private static int SENDERABSTAND = 0;

    private static int channels_in_list = 0;

    private static int actual_frequency = 0;

    public inet2RF(int channels_in_list) {
        this.channels_in_list = channels_in_list;
        SENDERABSTAND = (MAXIMAL_TX_FRQ - MINIMAL_TX_FRQ) / channels_in_list - 1;
    }

    public static int getChannelNoFromFreq(int frequency) {
        frequency += IF_OFFSET;
        actual_frequency = frequency;
        if (frequency >= MINIMAL_TX_FRQ && frequency <= MAXIMAL_TX_FRQ) {
            for (int i = 0; i < channels_in_list; i++) {
                if ((frequency >= ((MINIMAL_TX_FRQ + i * SENDERABSTAND) - (SENDERABSTAND / 2))) &&
                        (frequency <= ((MINIMAL_TX_FRQ + i * SENDERABSTAND) + (SENDERABSTAND / 2)))) {
                    return i;
                }
            }
        }
        return 0;
    }

    public static int getNewTXFrequency(int frequency) {
        frequency += IF_OFFSET;
        if (frequency >= MINIMAL_TX_FRQ && frequency <= MAXIMAL_TX_FRQ) {
            return (MINIMAL_TX_FRQ + getChannelNoFromFreq(frequency) * SENDERABSTAND);
        }
        return MINIMAL_TX_FRQ;
    }

    public static int getMinimalTxFrq() {
        return MINIMAL_TX_FRQ;
    }

    public static int getMaximalTxFrq() {
        return MAXIMAL_TX_FRQ;
    }

    public static int getSENDERABSTAND() {
        return SENDERABSTAND;
    }

    public static int getIFOffset() {
        return IF_OFFSET;
    }

    public static int getActual_frequency() {
        return actual_frequency;
    }
}

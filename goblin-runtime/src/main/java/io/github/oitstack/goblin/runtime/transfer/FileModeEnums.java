package io.github.oitstack.goblin.runtime.transfer;

/**
 * @Author CuttleFish
 * @Date 2022/6/6 下午5:01
 */
public enum FileModeEnums {
    FILE_000(0b1000000000000000),
    DIR_000(0b0100000000000000),
    FILE_644(0b1000000110100100),
    DIR_755(0b0100000111101101);

    private FileModeEnums(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    int mode;

}

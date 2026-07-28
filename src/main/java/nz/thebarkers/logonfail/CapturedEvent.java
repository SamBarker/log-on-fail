package nz.thebarkers.logonfail;

public record CapturedEvent(long nanoTime, String formattedLine) {
}

public enum TrackScale {
    N(new Length(9, Length.Unit.MM));
    
    private Length gauge;
    
    TrackScale(Length gauge) {
	this.gauge = gauge;
    }
    
    double ballastWidth() {
	return gauge.getPixels() * 1.4;
    }

    double tieLength() {
	return gauge.getPixels() * 1.2;
    }

    double railGauge() {
	return gauge.getPixels();
    }
    
}

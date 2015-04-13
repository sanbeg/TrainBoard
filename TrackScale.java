public enum TrackScale {
    //N(new Length(9, Length.Unit.MM));
    N(new Length(3.0/8, Length.Unit.IN));
    
    private Length gauge;
    
    TrackScale(Length gauge) {
	this.gauge = gauge;
    }
    
    double ballastWidth() {
	return gauge.getPixels() * 1.8;
    }

    double tieLength() {
	return gauge.getPixels() * 1.6;
    }
    double tieWidth() {
        return 3.0;
    }
    
    double railGauge() {
	return gauge.getPixels();
    }

    double railWidth() {
        return 2.0;
    }
    
}

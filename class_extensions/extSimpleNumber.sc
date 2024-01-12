+SimpleNumber {

	bilincurve { |inMin = -1, inMax = 1, outMin = -1, outMax = 1, curve = -4, clip = \minmax|
		var inCenter = inMin + inMax / 2;
		var outCenter = outMin + outMax / 2;

		^if (this < inCenter) {
			this.lincurve(inMin, inCenter, outMin, outCenter, curve.neg, clip)
		} {
			this.lincurve(inCenter, inMax, outCenter, outMax, curve, clip)
		}
	}

}
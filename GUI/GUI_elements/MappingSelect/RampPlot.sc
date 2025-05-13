RampPlot : SCViewHolder {
	// curveArray models a linear ramp;
	var curveArray = #[0.0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19, 0.2, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29, 0.3, 0.31, 0.32, 0.33, 0.34, 0.35, 0.36, 0.37, 0.38, 0.39, 0.4, 0.41, 0.42, 0.43, 0.44, 0.45, 0.46, 0.47, 0.48, 0.49, 0.5, 0.51, 0.52, 0.53, 0.54, 0.55, 0.56, 0.57, 0.58, 0.59, 0.6, 0.61, 0.62, 0.63, 0.64, 0.65, 0.66, 0.67, 0.68, 0.69, 0.7, 0.71, 0.72, 0.73, 0.74, 0.75, 0.76, 0.77, 0.78, 0.79, 0.8, 0.81, 0.82, 0.83, 0.84, 0.85, 0.86, 0.87, 0.88, 0.89, 0.9, 0.91, 0.92, 0.93, 0.94, 0.95, 0.96, 0.97, 0.98, 0.99, 1.0];
	// initEnv is a simple linear ramp: Env([0, 1], [1], \lin)
	var initEnv;

	*new { |parent, rect, ramp = \linlin, background(Color(0.0, 0.0, 0.1)), foreground(Color.green)|
		^super.new.init(parent, rect, ramp, background, foreground)
	}

	init { |parentView, rect, ramp, bgcolor, fgcolor|
		initEnv = Env([0, 1], [1], \lin);
		this.view = UserView(parentView, rect)
		.background_(bgcolor);

		switch (ramp.class)
		{ Event } {
			if (ramp.ramp.isNil or: { ramp.curve.isNil }) {
				"if arg 'ramp' is given as an Event it must contain two keys: 'ramp', denoting the mapping method (\\lincurve or \\linbicurve) and 'curve', a number denoting the curve caracteristics".error;
				ramp = \linlin;
			} {
				ramp.ramp = ramp.ramp.asSymbol;
				if (ramp.curve.isNumber) {
					switch (ramp.ramp)
					{ \lincurve } {
						ramp = curveArray.perform(ramp.ramp, 0, 1, 0, 1, ramp.curve, \minmax)
					}
					{ \linbicurve } {
						ramp = curveArray.perform(ramp.ramp, 0, 1, nil, 0, 1, nil, ramp.curve, \minmax)
					}
				}
			};

		}
		{ Env } {

		};
		this.view.drawFunc_({ |v|
		})
	}

	draw { |ramp|

	}

}
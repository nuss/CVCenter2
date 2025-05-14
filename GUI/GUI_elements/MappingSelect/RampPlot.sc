RampPlot : SCViewHolder {

	*new { |parent, rect, ramp = \linlin, background(Color(0.0, 0.0, 0.1)), foreground(Color.cyan)|
		^super.new.init(parent, rect, ramp, background, foreground)
	}

	init { |parentView, rect, ramp, bgcolor, fgcolor|
		var rampVals = this.prCreateRampVals(ramp);

		this.view = UserView(parentView, rect)
		.background_(bgcolor);

		this.view.drawFunc_({ |v|
			Pen.strokeColor_(fgcolor).moveTo(0@this.view.bounds.height);
			(rampVals.size - 1).do { |i|
				Pen.lineTo(Point(
					rampVals.size/this.view.bounds.width*(i+1),
					rampVals[i+1]*this.view.bounds.height
				).postln)
			};
			Pen.stroke;
		})
	}

	draw { |ramp|

	}

	prCreateRampVals { |ramp|
		// rampArray models a linear ramp;
		var rampArray = (0, 0.01..1);

		switch (ramp.class)
		{ Array } {
			if (ramp.size < 2) {
				"if arg 'ramp' is given as an Array it must contain two values: the first one denoting the mapping method (\\lincurve or \\linbicurve) and the second one a number denoting the curve caracteristics".error;
				^rampArray;
			} {
				ramp[0] = ramp[0].asSymbol;
				if (ramp[1].isNumber) {
					switch (ramp[0])
					{ \lincurve } {
						^rampArray.perform(ramp[0], 0, 1, 0, 1, ramp[1])
					}
					{ \linbicurve } {
						^rampArray.perform(ramp[0], 0, 1, nil, 0, 1, nil, ramp[1])
					}
				}
			}
		}
		{ Env } {
			if (ramp.respondsTo(\asMultichannelSignal).not) {
				"No valid envelope given - defaulting to Env([0, 1], [1], \lin)".error;
				^rampArray
			} {
				^rampArray.linenv(0, 1, 0, 1, ramp, \minmax, rampArray.size)
			}
		}
		{
			^rampArray.perform(ramp.asSymbol, 0, 1, 0, 1)
		}
	}

}
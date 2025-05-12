+SimpleNumber {

	linbicurve { |inMin = -1, inMax = 1, inCenter, outMin = -1, outMax = 1, outCenter, curve = -4, clip = \minmax|
		inCenter = inCenter ?? { inMin + inMax / 2 };
		outCenter = outCenter ?? { outMin + outMax / 2 };

		^if (this < inCenter) {
			this.lincurve(inMin, inCenter, outMin, outCenter, curve.neg, clip)
		} {
			this.lincurve(inCenter, inMax, outCenter, outMax, curve, clip)
		}
	}

	linenv { |inMin = 0, inMax = 1, outMin = 0, outMax = 1, env, clip = \minmax, resolution = 400|
		var envVals;
		if (env.isNil or: { env.respondsTo(\asMultichannelSignal).not }) {
			Error("No valid envelope given for linenv: %".format(env)).throw;
		};
		envVals = env.asMultichannelSignal(resolution, Array).unbubble;
		switch(clip,
			\minmax, {
				if(this <= inMin) { ^envVals.first * (outMax - outMin) + outMin };
				if(this >= inMax) { ^envVals.last * (outMax - outMin) + outMin };
			},
			\min, {
				if(this <= inMin) { ^envVals.first * (outMax - outMin) + outMin };
			},
			\max, {
				if(this >= inMax) { ^envVals.last * (outMax - outMin) + outMin };
			}
		);

		^envVals.blendAt(this / (inMax - inMin) * resolution) * (outMax - outMin) + outMin
	}

}

+SequenceableCollection {

	linbicurve { |...args|
		^this.multiChannelPerform(\linbicurve, *args)
	}

	linenv { |...args|
		^this.multiChannelPerform(\linenv, *args)
	}
}
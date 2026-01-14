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
		// input indexing must start at index 0 of the env array
		// if inMin is smaller than 0 we add inMin.neg
		// to garantee indexing starts at index 0
		// in contarary, if in inMin is bigger than 0 we substract inMin
		var posCorr = inMin.neg;

		if (env.isNil or: { env.respondsTo(\asMultichannelSignal).not }) {
			Error("No valid envelope given for method 'linenv': %".format(env)).throw;
		};


		envVals = env.asMultichannelSignal(resolution, Array).unbubble.normalize(outMin, outMax);
		switch(clip,
			\minmax, {
				if (this <= inMin) { ^envVals.first };
				if (this >= inMax) { ^envVals.last };
			},
			\min, {
				if (this <= inMin) { ^envVals.first };
			},
			\max, {
				if (this >= inMax) { ^envVals.last };
			}
		);

		^envVals.blendAt(this + posCorr / (inMax - inMin) * resolution)
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
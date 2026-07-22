
CVWidgetMS : CVWidget {
	var <cv, setup;
	// only needed for naming a connector
	var <>numOscConnectors = 0, <>numMidiConnectors = 0;

	*new { |name, cv, numSliders(5), setup, action, modelsAndControllers|
		if (cv.size < 1) {
			"Cannot create new CVWidgetMS from CV. Try CVWidgetKnob.new instead.".error;
			^nil;
		};
		^super.newCopyArgs(name, modelsAndControllers, cv: cv, setup: setup).init(action, numSliders);
	}

	init { |action, numSliders|
		name ?? {
			Error("No name provided for new CVWidgetKnob").throw;
		};

		name = name.asSymbol;

		cv ?? { cv = CV([0.0!numSliders, 1.0!numSliders].asSpec) };

		syncKeysEvent ?? {
			syncKeysEvent = (prto: List[\default], user: List[])
		};

		all[name] ?? { all.put[name, this] };
		// an Event to be used for variables defined outside actions
		env = ();
		// the functions that will be evaluated by a SimpleController that's added by calling addAction
		widgetActions = ();
		// the user-supplied actions, added as argument to addAction
		// the actions are evaluated within the outer widgetAction
		// userActions = ();
		// add a 'default' action, if given
		action !? { this.addAction(\default, action) };

		wmc = ();
		wmc.midiConnectors = (m: List[]);
		wmc.oscConnectors = (m: List[]);
		numSliders.do { |i|
			wmc.midiConnectors.m.add(Ref(List[]));
			wmc.oscConnectors.m.add(Ref(List[]));
		};

		// this.initConnectors(wmc);
		this.initModels(wmc);

		setup !? {
			if (setup.isArray.not or: {
				setup.isArray and: {
					setup.size != numSliders
				}
			}) {
				Error("A setup for a CVWidgetMS must be given as array with the same size of arg numSliders").throw
			};

			numSliders.do { |i|
				if (setup[i].isKindOf(Dictionary).not) {
					Error("Single slots in arg setup for a CVWidgetMS must be given as a Dictionary or an Event").throw
				} {
					setup[i][\midiMode] !? { this.setMidiMode(setup[i][\midiMode], i) };
					setup[i][\midiResolution] !? { this.setMidiResolution(setup[i][\midiResolution], i) };
					setup[i][\midiMean] !? { this.setMidiZero(setup[i][\midiMean], i) };
					setup[i][\midiCtrlButtonBank] !? { this.setMidiCtrlButtonGroup(setup[i][\midiCtrlButtonBank], i) };
					setup[i][\midiSnapDistance] !? { this.setMidiSnapDistance(setup[i][\midiSnapDistance], i) };
					setup[i][\midiInputMapping] !?	{ this.setMidiInputMapping(setup[i][\midiInputMapping], i) };
					setup[i][\oscCalibration] !? { this.setOscCalibration(setup[i][\oscCalibration], i) };
					setup[i][\oscInputRange] !? { this.setOscInputConstraints(setup[i][\oscInputRange], i) };
					setup[i][\oscInputMapping] !? { this.setOscInputMapping(setup[i][\oscInputMapping], i) };
					setup[i][\oscEndless] !? { this.setOscEndless(setup[i][\oscEndless], i) };
					setup[i][\oscResolution] !? { this.setOscResolution(setup[i][\oscResolution], i) };
					setup[i][\oscSnapDistance] !? { this.setOscSnapDistance(setup[i][\oscSnapDistance], i) };
					setup[i][\oscMatching] !? { this.setOscMatching(setup[i][\oscMatching], i) };
				}
			}
		}
	}

	// initConnectors {
	// 	wmc.midiConnectors ?? { wmc.midiConnectors = () };
	// 	wmc.midiConnectors.m ?? {
	// 		wmc.midiConnectors.m = List[];
	// 		this.size.do {
	// 			wmc.midiConnectors.m.add(Ref(List[]))
	// 		}
	// 	};
	// 	wmc.oscConnectors ?? { wmc.oscConnectors = () };
	// 	wmc.oscConnectors.m ?? {
	// 		wmc.oscConnectors.m = List[];
	// 		this.size.do {
	// 			wmc.oscConnectors.m.add(Ref(List[]))
	// 		}
	// 	}
	// }

	size {
		^this.getSpec.size;
	}
}
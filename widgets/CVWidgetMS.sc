
CVWidgetMS : CVWidget {
	var setup;
	// only needed for naming a connector
	var <>numOscConnectors, <>numMidiConnectors;

	*new { |name, cv, numSliders(5), setup, action|
		^super.newCopyArgs(name, cv: cv, setup: setup).init(action, numSliders);
	}

	init { |action, numSliders|
		name ?? {
			Error("No name provided for new CVWidgetKnob").throw;
		};

		name = name.asSymbol;

		this.cv ?? { cv = CV([0.0!numSliders, 1.0!numSliders].asSpec) };

		#numOscConnectors, numMidiConnectors = 0 ! this.cv.size ! 2;

		syncKeysEvent ?? {
			syncKeysEvent = (proto: List[\default], user: List[])
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

	initModels { |wmc|
		// models, not tied to connectors, global to all
		// MIDI and OSC connections
		wmc.cvSpec = (m: Ref(this.cv.spec));
		wmc.actions = (m: Ref((numActions: 0, activeActions: 0)));
		wmc.midiConnectors = (m: List[]);
		wmc.oscConnectors = (m: List[]);
		this.size.do { |i|
			wmc.midiConnectors.m.add(Ref(List[]));
			wmc.oscConnectors.m.add(Ref(List[]));
		};

		this.initControllers(wmc);

		// every new CVWidget should
		// immediately be amended by
		// an empty OscConnector
		// resp. an empty MidiConnector
		// controllers for connectors
		// are added within these classes
		this.size.do { |slot|
			OscConnectorMS(this, slot: slot);
			// MidiConnectorMS(this, slot: slot);
		}
	}

	midiConnectors {
		^wmc.midiConnectors.m.collect(_.value)
	}

	oscConnectors {
		^wmc.oscConnectors.m.collect(_.value)
	}

	size {
		^this.getSpec.size;
	}

		// the CV's ControlSpec
	setSpec { |spec|
		if ((spec = spec.asSpec).isKindOf(ControlSpec).not) {
			Error("No valid ControlSpec given for setSpec.").throw;
		};
		// expand spec if its size == 0. We're inside a CVWidgetMS.
		// Even a spec with size 1 is a multichannel spec.
		if (spec.size == 0) {
			spec = ControlSpec(
				spec.minval ! this.size,
				spec.maxval ! this.size,
				// SegWarp
				spec.warp ! this.size,
				spec.step ! this.size,
				spec.default ! this.size,
				spec.units, spec.grid
			)
		};
		wmc.cvSpec.m.value_(spec).changedPerformKeys(this.syncKeys);
	}

	midiDialog { |connector(0), slot(0), parent|
		^MidiConnectorsEditorView(this, connector, parent, slot).front;
	}

	oscDialog { |connector(0), slot(0), parent|
		^OscConnectorsEditorView(this, connector, parent, slot).front;
	}
}
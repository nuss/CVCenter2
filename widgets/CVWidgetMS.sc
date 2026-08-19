
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
			MidiConnectorMS(this, slot: slot);
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
		^MidiConnectorsEditorView(parent, this, slot, connector).front;
	}

	oscDialog { |connector(0), slot(0), parent|
		^OscConnectorsEditorView(parent, this, slot, connector).front;
	}

	// MIDI
	getMidiConnector { |connector, slot|
		case
		{ connector.isNumber and: { slot.isNumber }} {
			"getMidiConnector: connector.isNumber and: { slot.isNumber }".postln;
			^this.midiConnectors[slot.asInteger][connector.asInteger]
		}
		{ connector.isNumber and: { slot.isNil }} {
			"getMidiConnector: connector.isNumber and: { slot.isNil }".postln;
			^this.midiConnectors.collect(_[connector.asInteger])
		}
		{ connector.isNil and: { slot.isNumber }} {
			"getMidiConnector: connector.isNil and: { slot.notNil }".postln;
			^this.midiConnectors[slot]
		}
		{ connector.class === MidiConnectorMS and: {
			connector.widget === this
		}} {
			^connector
		};
		^nil
	}

	prMidiCasePerformArgsSet { |connector, slot, selector ... args, kwargs|
		case
		{ connector.isNil and: { slot.isNil }} {
			// "connector.isNil and: { slot.isNil }".postln;
			this.midiConnectors.do { |cons| cons.do(_.performArgs(selector, [], kwargs)) }
		}
		// if connector is given as a numeric index and no connector
		// at that index exists *all* connectors in the given slot
		// will be updated as providing a non existing connector is
		// the same as providing no connector at all
		{ connector.isNil and: { slot.notNil }} {
			// "connector.isNil and: { slot.notNil }".postln;
			this.midiConnectors[slot].do(_.performArgs(selector, [], kwargs))
		}
		{ connector.notNil and: { slot.isNil }} {
			// "connector.notNil and: { slot.isNil }".postln;
			this.midiConnectors.do(_.do { |con, i|
				if (connector[i] === con) { con.performArgs(selector, [], kwargs) }
			})
		}
		{ connector.notNil and: { slot.notNil }} {
			// "connector.notNil and: { slot.notNil }".postln;
			// "connector: %".format(connector).postln;
			connector.do(_.performArgs(selector, [], kwargs))
		}
	}

	prMidiCasePerformGet { |connector, slot, selector|
		// [connector, slot].postln;
		case
		{ connector.isNil and: { slot.isNil }} {
			// "connector.isNil and: { slot.isNil }".postln;
			^this.midiConnectors.collect { |cons, i| cons.collect(_.perform(selector, i)) }
		}
		// even if argument 'connector' has not been given, getMidiConnector should construct
		// the connector as long as a (vaild) slot has been given and a the call to
		// prMidiCasePerformGet should never end here
		{ connector.isNil and: { slot.notNil }} {
			// "connector.isNil and: { slot.notNil }".postln;
			// ^this.midiConnectors[slot].select(_.notNil).collect(_.perform(selector, slot))
			^nil
		}
		{ connector.notNil and: { slot.isNil }} {
			connector.postln;
			^this.midiConnectors.collect { |sl, i|
				sl.select { |con|
					con === connector[i]
				}.collect(_.perform(selector, i)).unbubble
			}
		}
		{ connector.notNil and: { slot.notNil }} {
			// "connector.notNil and: { slot.notNil }".postln;
			// "connector: %".format(connector).postln;
			if (connector.class === MidiConnectorMS) {
				^connector.perform(selector, slot)
			} {
				^connector.collect(_.perform(selector, slot))
			}
		}
	}

	setMidiMode { |mode, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMidiMode(mode)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMidiMode, mode: mode);
		}
	}

	getMidiMode { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMidiMode
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMidiMode);
		}
	}

	setMidiZero { |zeroval, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMidiZero(zeroval)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMidiZero, zeroval: zeroval);
		}
	}

	getMidiZero { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMidiZero
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMidiZero);
		}
	}

	setMidiSnapDistance { |snapDistance, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMidiSnapDistance(snapDistance)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMidiMode, snapDistance: snapDistance);
		}
	}

	getMidiSnapDistance { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMidiSnapDistance
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMidiSnapDistance);
		}
	}

	setMidiCtrlButtonGroup { |numButtons, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMidiCtrlButtonGroup(numButtons)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMidiCtrlButtonGroup, numButtons: numButtons);
		}
	}

	getMidiCtrlButtonGroup { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMidiCtrlButtonGroup
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMidiCtrlButtonGroup);
		}
	}

	setMidiResolution { |resolution, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMidiResolution(resolution)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMidiResolution, resolution: resolution);
		}
	}

	getMidiResolution { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMidiResolution
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMidiResolution);
		}
	}

	setMidiInputMapping { |mapping, curve, env, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMidiInputMapping(mapping, curve, env)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMidiInputMapping, mapping: mapping, curve: curve, env: env);
		}
	}

	getMidiInputMapping { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMidiInputMapping
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMidiInputMapping);
		}
	}

	setMiditemplate { |argTemplate, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMiditemplate(argTemplate)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMiditemplate, argTemplate: argTemplate);
		}
	}

	getMidiTemplate { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMidiTemplate
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMidiTemplate);
		}
	}

	setMidiDispatcher { |dispatcher, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMidiDispatcher(dispatcher)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMidiDispatcher, dispatcher: dispatcher);
		}
	}

	getMidiDispatcher { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMidiDispatcher
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMidiDispatcher);
		}
	}

	setMIDIFuncEnabled { |boolEnabled, connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			connector.setMIDIFuncEnabled(boolEnabled)
		} {
			connector = this.getMidiConnector(connector, slot);
			this.prMidiCasePerformArgsSet(connector, slot, \setMIDIFuncEnabled, boolEnabled: boolEnabled);
		}
	}

	getMIDIFuncEnabled { |connector, slot|
		if (connector.class === MidiConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getMIDIFuncEnabled
		} {
			connector = this.getMidiConnector(connector, slot);
			^this.prMidiCasePerformGet(connector, slot, \getMIDIFuncEnabled);
		}
	}

	midiConnect { |connector, slot, src, chan, num, argTemplate, dispatcher|
		if (connector.class === MidiConnectorMS and: { connector.widget === this }) {
			connector.midiConnect(num, chan, src, argTemplate, dispatcher)
		} {
			case
			{ connector.class === MidiConnectorMS } {
				Error("CVWidgetMS:-midiConnect: The given % doesn't belong to %! Cannot connect.".form(connector, this)).throw
			}
			{ connector.isNil and: { slot.isNil }} {
				"You have to either provide a valid MidiConnectorMS or a numeric slot to establish a MIDI connection in CVWidgetMS:-midiConnect".error;
				^this
			}
			{ slot.notNil } {
				if (slot.isNumber.not or: { slot >= this.size }) {
					"CVWidgetMS:-midiConnect: The given slot is invalied - must be numeric and smaller than the size of %: %.".format(this, this.size).error;
					^this
				} {
					slot = slot.asInteger;
					if (connector.isNil) {
						if (this.midiConnectors[slot].size == 1 and: {
							wmc.midiConnections.m[slot].value[0].isNil
						}) {
							connector = this.midiConnectors[slot][0]
						} {
							connector = MidiConnectorMS(this, slot: slot)
						}
					} {
						if (connector.isNumber) {
							if (this.wmc.midiConnections.m[slot].value[connector.asInteger].notNil) {
								"CVWidgetMS:-midiConnect: Connector % at slot % is already connected. Cannot connect".format(connector.asInteger, slot).error;
								^this
							}
						} {
							connector = this.getMidiConnector(connector, slot)
						}
					};
					connector.midiConnect(num, chan, src, argTemplate, dispatcher);
				}
			}
		}
	}

	midiDisconnect { |connector, slot|
		case
		{ connector.class === MidiConnectorMS and: {
			connector.widget === this
		}} {
			connector.midiDisconnect
		}
		{ connector.isNil and: { slot.isNumber }} {
			if (slot >= this.size) {
				"CVWidgetMS:-oscDisconnect: A slot must be given as an integer smaller than the widget's size: %".format(this.size).error
			} {
				this.midiConnectors[slot.asInteger].do(_.midiDisconnect)
			}
		}
		{ connector.isNumber and: {slot.isNil }} {
			this.getMidiConnector(connector.asInteger).do(_.midiDisconnect)
		}
		{ connector.isNil and: { slot.isNil }} {
			this.midiConnectors.collect(_.asArray).flat.do(_.midiDisconnect)
		}
	}

	addMidiConnector { |name, slot|
		slot ?? {
			"CVWidgetMS:-addMidiConnector: No slot given, cannot add MidiConnector".error;
			^nil
		};
		if (slot < this.size) {
			name !? { name = name.asSymbol };
			^MidiConnectorMS(this, name, slot.asInteger);
		} {
			"Can't add a MidiConnectorMS to a non-existing slot.".error;
			^nil
		}
	}

	removeMidiConnector { |connector, slot, forceAll = false|
		if (connector.isInteger) {
			connector = this.midiConnectors[slot][connector]
		};
		connector.remove(forceAll);
	}

	// OSC
	getOscConnector { |connector, slot|
		case
		{ connector.isNumber and: { slot.isNumber }} {
			"getOscConnector: connector.isNumber and: { slot.isNumber }".postln;
			^this.oscConnectors[slot.asInteger][connector.asInteger]
		}
		{ connector.isNumber and: { slot.isNil }} {
			"getOscConnector: connector.isNumber and: { slot.isNil }".postln;
			^this.oscConnectors.collect(_[connector.asInteger])
		}
		{ connector.isNil and: { slot.isNumber }} {
			"getOscConnector: connector.isNil and: { slot.notNil }".postln;
			^this.oscConnectors[slot]
		}
		{ connector.class === OscConnectorMS and: {
			connector.widget === this
		}} {
			^connector
		};
		^nil
	}

	prOscCasePerformArgsSet { |connector, slot, selector ... args, kwargs|
		case
		{ connector.isNil and: { slot.isNil }} {
			// "connector.isNil and: { slot.isNil }".postln;
			this.oscConnectors.do { |cons| cons.do(_.performArgs(selector, [], kwargs)) }
		}
		// if connector is given as a numeric index and no connector
		// at that index exists *all* connectors in the given slot
		// will be updated as providing a non existing connector is
		// the same as providing no connector at all
		{ connector.isNil and: { slot.notNil }} {
			// "connector.isNil and: { slot.notNil }".postln;
			this.oscConnectors[slot].do(_.performArgs(selector, [], kwargs))
		}
		{ connector.notNil and: { slot.isNil }} {
			// "connector.notNil and: { slot.isNil }".postln;
			this.oscConnectors.do(_.do { |con, i|
				if (connector[i] === con) { con.performArgs(selector, [], kwargs) }
			})
		}
		{ connector.notNil and: { slot.notNil }} {
			// "connector.notNil and: { slot.notNil }".postln;
			// "connector: %".format(connector).postln;
			connector.do(_.performArgs(selector, [], kwargs))
		}
	}

	prOscCasePerformGet { |connector, slot, selector|
		// [connector, slot].postln;
		case
		{ connector.isNil and: { slot.isNil }} {
			// "connector.isNil and: { slot.isNil }".postln;
			^this.oscConnectors.collect { |cons, i| cons.collect(_.perform(selector, i)) }
		}
		// even if argument 'connector' has not been given, getMidiConnector should construct
		// the connector as long as a (vaild) slot has been given and a the call to
		// prMidiCasePerformGet should never end here
		{ connector.isNil and: { slot.notNil }} {
			// "connector.isNil and: { slot.notNil }".postln;
			// ^this.oscConnectors[slot].select(_.notNil).collect(_.perform(selector, slot))
			^nil
		}
		{ connector.notNil and: { slot.isNil }} {
			connector.postln;
			^this.oscConnectors.collect { |sl, i|
				sl.select { |con|
					con === connector[i]
				}.collect(_.perform(selector, i)).unbubble
			}
		}
		{ connector.notNil and: { slot.notNil }} {
			// "connector.notNil and: { slot.notNil }".postln;
			// "connector: %".format(connector).postln;
			if (connector.class === OscConnectorMS) {
				^connector.perform(selector, slot)
			} {
				^connector.collect(_.perform(selector, slot))
			}
		}
	}

	setOscEndless { |boolEndless, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscEndless(boolEndless)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscEndless, boolEndless: boolEndless);
		}
	}

	getOscEndless { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscEndless
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscEndless);
		}
	}

	setOscResolution { |resolution, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscResolution(resolution)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscResolution, resolution: resolution);
		}
	}

	getOscResolution { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscResolution
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscResolution);
		}
	}

	setOscSnapDistance { |distance, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscSnapDistance(distance)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscSnapDistance, distance: distance);
		}
	}

	getOscSnapDistance { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscSnapDistance
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscSnapDistance);
		}
	}

	setOscCalibration { |boolCalibration, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscCalibration(boolCalibration)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscCalibration, boolCalibration: boolCalibration);
		}
	}

	getOscCalibration { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscCalibration
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscCalibration);
		}
	}

	resetOscCalibration { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.resetOscCalibration
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \resetOscCalibration);
		}
	}

	setOscInputMapping { |mapping, curve, env, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscInputMapping(mapping, curve, env)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscInputMapping, mapping: mapping, curve: curve, env: env);
		}
	}

	getOscInputMapping { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscInputMapping
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscInputMapping);
		}
	}

	setOscInputConstraints { |constraintsPair, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscInputConstraints(constraintsPair)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscInputConstraints, constraintsPair: constraintsPair);
		}
	}

	getOscInputConstraints { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscInputConstraints
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscInputConstraints);
		}
	}

	setOscMatching { |boolMatching, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscMatching(boolMatching)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscMatching, boolMatching: boolMatching);
		}
	}

	getOscMatching { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscMatching
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscMatching);
		}
	}

	setOscInputAlwaysPositive { |value, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscInputAlwaysPositive(value)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscInputAlwaysPositive, value: value);
		}
	}

	getOscInputAlwaysPositive { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscInputAlwaysPositive
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscInputAlwaysPositive);
		}
	}

	setOscCmdName { |cmdPath, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscCmdName(cmdPath)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscCmdName, cmdPath: cmdPath);
		}
	}

	getOscCmdName { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscCmdName
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscCmdName);
		}
	}

	setOscMsgIndex { |msgIndex, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscMsgIndex(msgIndex)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscMsgIndex, msgIndex: msgIndex);
		}
	}

	getOscMsgIndex { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscMsgIndex
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscMsgIndex);
		}
	}

	setOscTemplate { |argTemplate, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscTemplate(argTemplate)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscTemplate, msgIndex: argTemplate);
		}
	}

	getOscTemplate { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscTemplate
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscTemplate);
		}
	}

	setOscDispatcher { |dispatcher, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOscDispatcher(dispatcher)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOscDispatcher, dispatcher: dispatcher);
		}
	}

	getOscDispatcher { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOscDispatcher
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOscDispatcher);
		}
	}

	setOSCFuncEnabled { |boolEnabled, connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			connector.setOSCFuncEnabled(boolEnabled)
		} {
			connector = this.getOscConnector(connector, slot);
			this.prOscCasePerformArgsSet(connector, slot, \setOSCFuncEnabled, boolEnabled: boolEnabled);
		}
	}

	getOSCFuncEnabled { |connector, slot|
		if (connector.class === OscConnectorMS and: {
			connector.widget === this
		}) {
			^connector.getOSCFuncEnabled
		} {
			connector = this.getOscConnector(connector, slot);
			^this.prOscCasePerformGet(connector, slot, \getOSCFuncEnabled);
		}
	}

	oscConnect { |connector, slot, addr, cmdPath, oscMsgIndex(1), recvPort, argTemplate, dispatcher, matching(false)|
		if (connector.class === OscConnectorMS and: { connector.widget === this }) {
			connector.oscConnect(addr, cmdPath, oscMsgIndex, recvPort, argTemplate, dispatcher, matching)
		} {
			case
			{ connector.class === OscConnectorMS } {
				Error("CVWidgetMS:-oscConnect: The given % doesn't belong to %! Cannot connect.".form(connector, this)).throw
			}
			{ connector.isNil and: { slot.isNil }} {
				"You have to either provide a valid OscConnectorMS or a numeric slot to establish a OSC connection in CVWidgetMS:-oscConnect".error;
				^this
			}
			{ slot.notNil } {
				if (slot.isNumber.not or: { slot >= this.size }) {
					"CVWidgetMS:-oscConnect: The given slot is invalied - must be numeric and smaller than the size of %: %.".format(this, this.size).error;
					^this
				} {
					slot = slot.asInteger;
					if (connector.isNil) {
						if (this.oscConnectors[slot].size == 1 and: {
							wmc.oscConnections.m[slot].value[0].isNil
						}) {
							connector = this.oscConnectors[slot][0]
						} {
							connector = OscConnectorMS(this, slot: slot)
						}
					} {
						if (connector.isNumber) {
							if (this.wmc.oscConnections.m[slot].value[connector.asInteger].notNil) {
								"CVWidgetMS:-oscConnect: Connector % at slot % is already connected. Cannot connect".format(connector.asInteger, slot).error;
								^this
							}
						} {
							connector = this.getOscConnector(connector, slot)
						}
					};
					connector.oscConnect(addr, cmdPath, oscMsgIndex, recvPort, argTemplate, dispatcher, matching);
				}
			}
		}

	}

	oscDisconnect { |connector, slot|
		case
		{ connector.class === OscConnectorMS and: {
			connector.widget === this
		}} {
			connector.oscDisconnect
		}
		{ connector.isNil and: { slot.isNumber }} {
			if (slot >= this.size) {
				"CVWidgetMS:-oscDisconnect: A slot must be given as an integer smaller than the widget's size: %".format(this.size).error
			} {
				this.oscConnectors[slot.asInteger].do(_.oscDisconnect)
			}
		}
		{ connector.isNumber and: {slot.isNil }} {
			this.getOscConnector(connector.asInteger).do(_.oscDisconnect)
		}
		{ connector.isNil and: { slot.isNil }} {
			this.oscConnectors.collect(_.asArray).flat.do(_.oscDisconnect)
		}
	}

	// connections handling
	addOscConnector { |name, slot|
		slot ?? {
			"CVWidgetMS:-addConnector: No slot given, cannot add MidiConnector".error;
			^nil
		};
		if (slot < this.size) {
			name !? { name = name.asSymbol };
			^OscConnectorMS(this, name, slot.asInteger);
		} {
			"Can't add a OscConnectorMS to a non-existing slot.".error;
			^nil
		}
	}

	removeOscConnector { |connector, slot, forceAll = false|
		if (connector.isInteger) {
			connector = this.oscConnectors[slot][connector]
		};
		connector.remove(forceAll);
	}

	// widget specific
	// TODO
	remove {
		this.midiConnectors.reverse.do(_.remove(true));
		this.oscConnectors.reverse.do(_.remove(true));
		// SimpleControllers should be removed explicitely
		this.widgetActions.do { |asoc|
			asoc.key.remove;
		};
		// remove the widget's controllers from Object.dependantsDictionary
		this.wmc.do { |val|
			if (val.class === Event) { val.c.remove };
			if (val.class === List) { val.do { |it|
				it.pairsDo { |k, v| v.release };
			}}
		};
		all.removeAt(name);
	}

	// // init controllers (private)
	// prInitSpecControl { |wmc, cv|
	// 	wmc.cvSpec.c ?? {
	// 		wmc.cvSpec.c = SimpleController(wmc.cvSpec.m);
	// 	};
	// 	wmc.cvSpec.c.put(\default, { |changer, what, moreArgs|
	// 		this.cv.spec_(changer.value);
	// 	})
	// }
	//
	// prInitActionsControl { |wmc, cv|
	// 	wmc.actions.c ?? {
	// 		wmc.actions.c = SimpleController(wmc.actions.m);
	// 	};
	// 	wmc.actions.c.put(\default, { |changer, what, moreArgs|
	// 		// do something with changer.value
	// 	})
	// }

	storeOn { |stream|
		stream << this.class.name << "(" <<* [
			this.name.cs,
			this.cv
		] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}
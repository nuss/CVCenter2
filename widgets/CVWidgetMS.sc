
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
		{ connector.isNumber and: { slot.notNil }} {
			"getMidiConnector: connector.isNumber and: { slot.notNil }".postln;
			^this.midiConnectors[slot][connector.asInteger]
		}
		{ connector.isNumber and: { slot.isNil }} {
			"getMidiConnector: connector.isNumber and: { slot.isNil }".postln;
			^this.midiConnectors.collect(_[connector.asInteger])
		}
		{ connector.isNil and: { slot.notNil }} {
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
		[connector, slot].postln;
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
			// case
			// { connector.isNil and: { slot.isNil }} {
			// 	this.midiConnectors.do { |cons| cons.do(_.setMidiMode(mode)) }
			// }
			// // if connector is given as a numeric index and no connector
			// // at that index exists *all* connectors in the given slot
			// // will be updated as providing a non existing connector is
			// // the same as providing no connector at all
			// { connector.isNil and: { slot.notNil }} {
			// 	this.midiConnectors[slot].do(_.setMidiMode(mode))
			// }
			// { connector.notNil and: { slot.isNil }} {
			// 	this.midiConnectors.do(_.do { |con, i|
			// 		if (connector[i] === con) { con.setMidiMode(mode) }
			// 	})
			// }
			// { connector.notNil and: { slot.notNil }} {
			// 	connector.setMidiMode(mode)
			// }
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
			// case
			// { connector.isNil and: { slot.isNil }} {
			// 	^this.midiConnectors.collect(_.collect(_.getMidiMode))
			// }
			// { connector.isNil and: { slot.notNil }} {
			// 	^this.midiConnectors[slot].collect(_.getMidiMode)
			// }
			// { connector.notNil and: { slot.isNil }} {
			// 	connector.postln;
			// 	^this.midiConnectors.collect { |sl, i|
			// 		sl.select { |con|
			// 			con === connector[i]
			// 		}.collect(_.getMidiMode).unbubble
			// 	}
			// }
			// { connector.notNil and: { slot.notNil }} {
			// 	^connector[slot].getMidiMode
			// }
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

	midiConnect { |connector, src, chan, num, argTemplate, dispatcher|
		// create new annonymous connector if none is given
		connector ?? {
			if (this.midiConnectors.size == 1 and: {
				wmc.midiConnections.m.value[0].isNil
			}) {
				connector = this.midiConnectors[0]
			} {
				connector = MidiConnector(this)
			}
		};
		connector = this.getMidiConnector(connector);
		// pass execution to connector
		connector.midiConnect(src, chan, num, argTemplate, dispatcher);
	}

	midiDisconnect { |connector|
		connector ?? {
			Error("No connector given. Don't know which connector to disconnect.").throw;
		};
		connector = this.getMidiConnector(connector);
		// pass execution to connector
		connector.midiDisconnect
	}

	// OSC
	getOscConnector { |connector|
		if (connector.isInteger) {
			^this.oscConnectors[connector]
		};
		^connector
	}

	setOscEndless { |boolEndless, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscEndless(boolEndless))
		} {
			connector.setOscEndless(boolEndless)
		}
	}

	getOscEndless { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscEndless);
		} {
			^connector.getOscEndless;
		}
	}

	setOscResolution { |resolution, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscResolution(resolution))
		} {
			connector.setOscResolution(resolution)
		}
	}

	getOscResolution { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscResolution);
		} {
			^connector.getOscResolution;
		}
	}

	setOscSnapDistance { |distance, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscSnapDistance(distance))
		} {
			connector.setOscSnapDistance(distance)
		}
	}

	getOscSnapDistance { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscSnapDistance);
		} {
			^connector.getOscSnapDistance;
		}
	}

	setOscCalibration { |boolCalibration, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscCalibration(boolCalibration))
		} {
			connector.setOscCalibration(boolCalibration)
		}
	}

	getOscCalibration { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscCalibration)
		} {
			^connector.getOscCalibration;
		}
	}

	resetOscCalibration { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.resetOscCalibration)
		} {
			connector.resetOscCalibration
		}
	}

	setOscInputMapping { |mapping, curve, env, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscInputMapping(mapping, curve, env))
		} {
			connector.setOscInputMapping(mapping, curve, env)
		}
	}

	getOscInputMapping { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscInputMapping);
		} {
			^connector.getOscInputMapping;
		}
	}

	setOscInputConstraints { |constraintsPair, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscInputConstraints(constraintsPair))
		} {
			connector.setOscInputConstraints(constraintsPair)
		}
	}

	getOscInputConstraints { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscInputConstraints);
		} {
			^connector.getOscInputConstraints;
		}
	}

	setOscMatching { |boolMatching, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscMatching(boolMatching))
		} {
			connector.setOscMatching(boolMatching)
		}
	}

	getOscMatching { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscMatching);
		} {
			^connector.getOscMatching;
		}
	}

	setOscInputAlwaysPositive { |value, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscInputAlwaysPositive(value))
		} {
			connector.setOscInputAlwaysPositive(value)
		}
	}

	getOscInputAlwaysPositive { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscInputAlwaysPositive);
		} {
			^connector.getOscInputAlwaysPositive;
		}
	}

	setOscCmdName { |cmdPath, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscCmdName(cmdPath))
		} {
			connector.setOscCmdName(cmdPath)
		}
	}

	getOscCmdName { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscCmdName);
		} {
			^connector.getOscCmdName;
		}
	}

	setOscMsgIndex { |msgIndex, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscMsgIndex(msgIndex))
		} {
			connector.setOscMsgIndex(msgIndex)
		}
	}

	getOscMsgIndex { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscMsgIndex);
		} {
			^connector.getOscMsgIndex;
		}
	}

	setOscTemplate { |argTemplate, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscTemplate(argTemplate))
		} {
			connector.setOscTemplate(argTemplate)
		}
	}

	getOscTemplate { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscTemplate)
		} {
			^connector.getOscTemplate
		}
	}

	setOscDispatcher { |dispatcher, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOscDispatcher(dispatcher))
		} {
			connector.setOscDispatcher(dispatcher)
		}
	}

	getOscDispatcher { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOscDispatcher)
		} {
			^connector.getOscDispatcher
		}
	}

	setOSCFuncEnabled { |boolEnabled, connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			this.oscConnectors.do(_.setOSCFuncEnabled(boolEnabled))
		} {
			connector.setOSCFuncEnabled(boolEnabled)
		}
	}

	getOSCFuncEnabled { |connector|
		connector = this.getOscConnector(connector);
		if (connector.isNil) {
			^this.oscConnectors.collect(_.getOSCFuncEnabled)
		} {
			^connector.getOSCFuncEnabled
		}
	}

	oscConnect { |connector, addr, cmdPath, oscMsgIndex = 1, recvPort, argTemplate, dispatcher, matching = false|
		// create new annonymous connector if none is given
		connector ?? {
			if (this.oscConnectors.size == 1 and: {
				wmc.oscConnections.m.value[0].isNil
			}) {
				connector = this.oscConnectors[0]
			} {
				connector = OscConnector(this)
			}
		};
		connector = this.getOscConnector(connector);
		// pass execution to connector
		connector.oscConnect(addr, cmdPath, oscMsgIndex, recvPort, argTemplate, dispatcher, matching);
	}

	oscDisconnect { |connector|
		connector ?? {
			Error("No connector given. Don't know which connector to disconnect.").throw;
		};
		connector = this.getOscConnector(connector);
		// pass execution to connector
		connector.oscDisconnect
	}

	// connections handling
	addOscConnector { |name, slot|
		if (slot < this.size) {
			name !? { name = name.asSymbol };
			^OscConnectorMS(this, name, slot);
		} {
			"Can't add a MidiConnectorMS to a non-existing slot.".error;
			^nil
		}
	}

	removeOscConnector { |connector, slot, forceAll = false|
		if (connector.isInteger) {
			connector = this.midiConnectors[slot][connector]
		};
		connector.remove(forceAll);
	}

	addMidiConnector { |name, slot|
		if (slot < this.size) {
			name !? { name = name.asSymbol };
			^MidiConnectorMS(this, name, slot);
		} {
			"Can't add a OscConnectorMS to a non-existing slot.".error;
			^nil
		}
	}

	removeMidiConnector { |connector, slot, forceAll = false|
		if (connector.isInteger) {
			connector = this.midiConnectors[slot][connector]
		};
		connector.remove(forceAll);
	}

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
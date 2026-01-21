OscConnector {
	classvar cAnons = 0, <accum;
	classvar <onConnectorRemove;
	var <widget;
	var <alwaysPositive = 0.1;

	*initClass {
		accum = ();
	}

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("An OscConnector can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget).init(name);
	}

	*onConnectorRemove_ { |func|
		onConnectorRemove = onConnectorRemove.addFunc(func)
	}

	init { |name|
		widget.numOscConnectors = widget.numOscConnectors + 1;
		name ?? {
			name = "OSC Connection %".format(widget.numOscConnectors).asSymbol;
		};
		this.initModels(widget.wmc, name);
		widget.wmc.oscConnectors.m.value_(
			widget.wmc.oscConnectors.m.value.add(this)
		).changedPerformKeys(widget.syncKeys);
	}

	initModels { |wmc, name|
		wmc.oscConnections ?? { wmc.oscConnections = () };
		wmc.oscConnections.m ?? {
			wmc.oscConnections.m = Ref(List[]);
		};
		wmc.oscConnections.m.value.add(nil);

		wmc.oscDisplay ?? { wmc.oscDisplay = () };
		wmc.oscDisplay.m ?? {
			wmc.oscDisplay.m = Ref(List[]);
		};
		wmc.oscDisplay.m.value.add((
			nameField: '/my/cmd/name',
			index: 1,
			connectorButVal: 0,
			// editEnabled: true,
			connect: "learn"
		));

		wmc.oscOptions ?? { wmc.oscOptions = () };
		wmc.oscOptions.m ?? {
			wmc.oscOptions.m = Ref(List[])
		};
		wmc.oscOptions.m.value.add((
			oscEndless: CVWidget.oscEndless,
			oscResolution: CVWidget.resolution,
			oscCalibration: CVWidget.oscCalibration,
			oscSnapDistance: CVWidget.snapDistance,
			oscInputMapping: CVWidget.inputMapping,
			oscInputRange: CVWidget.oscInputRange,
			oscMatching: CVWidget.oscMatching
		));

		wmc.oscConnectorNames ?? { wmc.oscConnectorNames = () };
		wmc.oscConnectorNames.m ?? {
			wmc.oscConnectorNames.m = Ref(List[]);
		};
		wmc.oscConnectorNames.m.value.add(name);

		wmc.oscInputConstrainters ?? {
			wmc.oscInputConstrainters = List[];
		};
		wmc.oscInputConstrainters.add((
			lo: CV([-inf, inf].asSpec, CVWidget.oscInputRange[0]),
			hi: CV([-inf, inf].asSpec, CVWidget.oscInputRange[1])
		));

		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitOscConnections,
			prInitOscDisplay,
			prInitOscConnectors,
			prInitOscOptions,
			prInitOscConnectorNames
		].do { |method|
			this.perform(method, wmc, widget.cv)
		}
	}

	prInitOscConnectors { |mc, cv|
		mc.oscConnectors.c ?? {
			mc.oscConnectors.c = SimpleController(mc.oscConnectors.m)
		};
		mc.oscConnectors.c.put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	prInitOscConnections { |mc, cv|
		mc.oscConnections.c ?? {
			mc.oscConnections.c = SimpleController(mc.oscConnections.m)
		};
		mc.oscConnections.c.put(\default, { |changer, what, moreArgs|
			// do something...
		})
	}

	prInitOscDisplay { |mc, cv|
		mc.oscDisplay.c ?? {
			mc.oscDisplay.c = SimpleController(mc.oscDisplay.m)
		};
		mc.oscDisplay.c.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscOptions { |mc, cv|
		mc.oscOptions.c ?? {
			mc.oscOptions.c = SimpleController(mc.oscOptions.m)
		};
		mc.oscOptions.c.put(\default, { |changer, what, moreArgs|
			// do something
		})
	}

	prInitOscConnectorNames { |mc, cv|
		mc.oscConnectorNames.c ?? {
			mc.oscConnectorNames.c = SimpleController(mc.oscConnectorNames.m)
		};
		mc.oscConnectorNames.c.put(\default, { |changer, what, moreArgs|
			//  do something
		})
	}

	index {
		^widget.oscConnectors.indexOf(this);
	}

	name {
		^widget.wmc.oscConnectorNames.m.value[this.index];
	}

	name_ { |name|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscConnectorNames.m.value[index] = name.asSymbol;
		mc.oscConnectorNames.m.changedPerformKeys(widget.syncKeys, index);
	}

	setOscEndless { |boolEndless|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscOptions.m.value[index].oscEndless = boolEndless;
		mc.oscOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscEndless {
		^widget.wmc.oscOptions.m.value[this.index].oscEndless;
	}

	setOscResolution { |resolution|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscOptions.m.value[index].oscResolution = resolution;
		mc.oscOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscResolution {
		^widget.wmc.oscOptions.m.value[this.index].oscResolution;
	}

	setOscSnapDistance { |distance|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscOptions.m.value[index].oscSnapDistance = distance;
		mc.oscOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscSnapDistance {
		^widget.wmc.oscOptions.m.value[this.index].oscSnapDistance;
	}

	setOscCalibration { |boolCalibration|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscOptions.m.value[index].oscCalibration = boolCalibration;
		mc.oscOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscCalibration {
		^widget.wmc.oscOptions.m.value[this.index].oscCalibration;
	}

	resetOscCalibration {
		var index = this.index;
		var mc = widget.wmc;
		mc.oscOptions.m.value[index].oscInputRange = CVWidget.oscInputRange;
		mc.oscOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	setOscInputConstraints { |constraintsPair|
		var index = this.index;
		var mc = widget.wmc;
		var lo, hi;

		if (constraintsPair.class === Point) {
			lo = constraintsPair.x;
			hi = constraintsPair.y;
		} {
			#lo, hi = constraintsPair;
		};
		mc.oscInputConstrainters[index].lo.value_(lo);
		mc.oscInputConstrainters[index].hi.value_(hi);

		mc.oscOptions.m.value[index].oscInputRange = [lo, hi];
		mc.oscOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscInputConstraints {
		^widget.wmc.oscOptions.m.value[this.index].oscInputRange;
	}

	setOscInputMapping { |mapping, curve = 0, env(Env([0, 1], [1]))|
		var index = this.index;
		var mc = widget.wmc;
		mapping = mapping.asSymbol;
		[\linlin, \linexp, \explin, \expexp, \lincurve, \linbicurve, \linenv].indexOf(mapping) ?? {
			"arg 'mapping' must be one of \\linlin, \\linexp, \\explin, \\expexp, \\lincurve, \\linbicurve or \\linenv".error;
			^this
		};
		// special care needs to be taken to NOT set CVWidget.inputMapping
		// not working, would set CVWidget.inputMapping too:
		// mc..oscOptions.m.value[index].oscInputMapping.mapping = mapping;
		mc.oscOptions.m.value[index].oscInputMapping_((mapping: mapping));
		case
		{ mapping === \lincurve or: { mapping === \linbicurve }} {
			mc.oscOptions.m.value[index].oscInputMapping.curve = curve;
		}
		{ mapping === \linenv } {
			mc.oscOptions.m.value[index].oscInputMapping.env = env;
		};
		mc.oscOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscInputMapping {
		^widget.wmc.oscOptions.m.value[this.index].oscInputMapping;
	}

	setOscCmdName { |cmdPath|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscDisplay.m.value[index].nameField = cmdPath.asSymbol;
		mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscCmdName {
		^widget.wmc.oscDisplay.m.value[this.index].nameField;
	}

	setOscMsgIndex { |msgIndex|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscDisplay.m.value[index].index = msgIndex.asInteger;
		mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, index)
	}

	getOscMsgIndex {
		^widget.wmc.oscDisplay.m.value[this.index].index;
	}

	setOscMatching { |boolMatching|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscOptions.m.value[index].oscMatching = boolMatching;
		mc.oscOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscMatching {
		^widget.wmc.oscOptions.m.value[this.index].oscMatching;
	}

	setOscTemplate { |argTemplate|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscDisplay.m.value[index].oscTemplate = argTemplate;
		mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscTemplate {
		^widget.wmc.oscDisplay.m.value[this.index].oscTemplate;
	}

	setOscDispatcher { |dispatcher|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscDisplay.m.value[index].dispatcher = dispatcher;
		mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, index);
	}

	getOscDispatcher {
		^widget.wmc.oscDisplay.m.value[this.index].dispatcher;
	}

	oscConnect { |addr, cmdPath, oscMsgIndex = 1, recvPort, argTemplate, dispatcher, matching = false|
		var index = this.index;
		var mc = widget.wmc;
		mc.oscConnections.m.value[index] = this.prOSCFunc(addr, cmdPath, oscMsgIndex, recvPort, argTemplate, dispatcher, matching);
		// "mc.oscConnections.m.value[%]: %".format(index, mc.oscConnections.m.value[index]).postln;
		mc.oscConnections.m.changedPerformKeys(widget.syncKeys, index);
		mc.oscDisplay.m.value[index].ipField = mc.oscConnections.m.value[index].srcID.ip.asSymbol;
		mc.oscDisplay.m.value[index].portField = mc.oscConnections.m.value[index].srcID.port;
		mc.oscDisplay.m.value[index].nameField = mc.oscConnections.m.value[index].path;
		mc.oscDisplay.m.value[index].template = mc.oscConnections.m.value[index].argTemplate;
		mc.oscDisplay.m.value[index].dispatcher = mc.oscConnections.m.value[index].dispatcher;
		mc.oscDisplay.m.value[index].connectorButVal = 1;
		mc.oscDisplay.m.value[index].connect = "disconnect";
		mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? { this.oscDisconnect }
		})
	}

	oscDisconnect {
		var index = this.index;
		var mc = widget.wmc;
		mc.oscConnections.m.value[index].clear;
		mc.oscConnections.m.value[index] = nil;
		mc.oscConnections.m.changedPerformKeys(widget.syncKeys, index);
		// mc.oscDisplay.m.value[index].ipField = nil;
		// mc.oscDisplay.m.value[index].portField = nil;
		// mc.oscDisplay.m.value[index].template = nil;
		mc.oscDisplay.m.value[index].dispatcher = nil;
		mc.oscDisplay.m.value[index].connectorButVal = 0;
		mc.oscDisplay.m.value[index].connect = "connect";
		mc.oscDisplay.m.changedPerformKeys(widget.syncKeys, index);
		CmdPeriod.remove({
			this.widget !? { this.oscDisconnect }
		})
	}

	prOSCFuncAction { |mid|
		var input, cv = widget.cv, constraints, inputMapping, argValues;
		var snapDistance, constraintsRange;

		^{ |msg, time, addr, port|
			input = msg[mid ?? { this.getOscMsgIndex }];
			if (input <= 0 and: { input.abs > alwaysPositive }) {
				alwaysPositive = input.abs + 0.1
			};

			// FIXME: should input consider alwaysPositive correction??
			constraints = this.getOscInputConstraints;
			if (this.getOscCalibration) {
				// input constraints low
				if (input < constraints[0]) {
					this.setOscInputConstraints([input, constraints[1]])
				};
				// input constraints hi
				if (input > constraints[1]) {
					this.setOscInputConstraints([constraints[0], input])
				}
			};

			// "constraints: %".format(this.getOscInputConstraints).postln;

			inputMapping = this.getOscInputMapping;
			argValues = [
				inputMapping.mapping,
				constraints[0] + alwaysPositive,
				constraints[1] + alwaysPositive,
				this.widget.getSpec.minval,
				this.widget.getSpec.maxval
			];

			case
			{ inputMapping.mapping === \lincurve or: {
				inputMapping.mapping === \linbicurve
			}} {
				argValues = argValues.add(inputMapping.curve)
			}
			{ inputMapping.mapping === \linenv } {
				argValues = argValues.add(inputMapping.env)
			};

			argValues = argValues.add(\minmax);
			// "argValues: %".format(argValues).postln;

			constraintsRange = (constraints[1] - constraints[0]).abs;
			if (this.getOscEndless.not) {
				snapDistance = this.getOscSnapDistance;
				// unlike MIDI OSC values come in within a dynamic range
				// hence, we need to normalize based on this dynamic range
				// input must be positive, ranging from 0-1
				// [input, input+alwaysPositive, input/constraintsRange, (input+alwaysPositive)/constraintsRange].postln;
				if (constraintsRange == 0) { input = 0 } {
					input = input+alwaysPositive
				};
				// "input: %".format(input).postln;
				if ((snapDistance <= 0).or(
					input < (cv.input + (snapDistance/2)) and: {
						input > (cv.value - (snapDistance/2))
					}
				)) {
					case
					{ inputMapping.mapping === \lincurve } {
						if (inputMapping.curve != 0 and: { snapDistance > 0 }) {
							this.setOscSnapDistance(0)
						};
					};
					cv.value_(input.perform(*argValues));
					// "cv.value: %\n".format(cv.value).postln;
				};
				accum[widget] = cv.input;
			} {
				accum[widget] = accum[widget] + (input / constraintsRange / 32 * this.getOscResolution);

				case
				{ accum[widget] < 0 } { accum[widget] = 0 }
				{ accum[widget] > 1 } { accum[widget] = 1 };

				// [input, accum[widget], inputMapping, this.getOscResolution].postln;

				case
				{ inputMapping.mapping === \lincurve } {
					cv.input_(accum[widget].lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
				}
				{ inputMapping.mapping === \linbicurve } {
					cv.input_(accum[widget].linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
				}
				{ inputMapping.mapping === \linenv } {
					cv.input_(accum[widget].linenv(env: inputMapping.env))
				}
				{ inputMapping.mapping === \explin } {
					cv.input_((accum[widget]+1).explin(1, 2, 0, 1))
				}
				{ inputMapping.mapping === \expexp or: { inputMapping.mapping === \linexp }} {
					if (widget.getSpec.hasZeroCrossing) {
						this.setOscInputMapping(\linlin);
						cv.input_(accum[widget])
					} {
						cv.value_((accum[widget]+1).perform(inputMapping.mapping, 1, 2, widget.getSpec.minval, widget.getSpec.maxval))
					}
				}
				{ cv.input_(accum[widget]) };
			}
		}
	}

	prOSCFunc { |a, c, mid, r, t, d, m|
		// [a, c, mid, r, t, d].postln;
		accum[widget] = widget.cv.input;
		^if (m) {
			^OSCFunc.newMatching(this.prOSCFuncAction(mid), c, a, r, t)
		} {
			^OSCFunc(this.prOSCFuncAction(mid), c, a, r, t, d)
		}
	}

	remove { |forceAll = false|
		var mc = widget.wmc;
		// var wmc = CVWidget.wmc;
		var index = this.index;

		if (mc.oscConnectors.m.value.size > 1 or: { forceAll }) {
			this.oscDisconnect;
			// allOscFuncs??
			[
				mc.oscDisplay.m.value,
				mc.oscConnections.m.value,
				mc.oscConnectorNames.m.value,
				mc.oscOptions.m.value
			].do(_.removeAt(index));
			mc.oscConnectors.m.value.remove(this);
			mc.oscConnectors.m.changedPerformKeys(widget.syncKeys, index);
			onConnectorRemove.value(widget, index);
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [widget.name.cs, this.name] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}

MidiConnector {
	classvar cAnons = 0;
	classvar allMidiFuncs;
	classvar accum;
	classvar <onConnectorRemove;
	var <widget;

	*initClass {
		// allMidiFuncs = ();
		// input accumulation of input in a linear range in 'endless' mode
		// see ccAction in prInitMidiConnection
		accum = ();
	}

	*onConnectorRemove_ { |func|
		onConnectorRemove = onConnectorRemove.addFunc(func)
	}

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("A MidiConnector can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget).init(name);
	}

	init { |name|
		widget.numMidiConnectors = widget.numMidiConnectors + 1;
		name ?? {
			name = "MIDI Connection %".format(widget.numMidiConnectors).asSymbol;
		};
		this.initModels(widget.wmc, name);
		widget.wmc.midiConnectors.m.value_(
			widget.wmc.midiConnectors.m.value.add(this)
		).changedPerformKeys(widget.syncKeys);
	}

	initModels { |wmc, name|
		wmc.midiOptions ?? { wmc.midiOptions = () };
		wmc.midiOptions.m ?? {
			wmc.midiOptions.m = Ref(List[]);
		};
		wmc.midiOptions.m.value.add((
			midiMode: CVWidget.midiMode,
			midiZero: CVWidget.midiZero,
			ctrlButtonGroup: CVWidget.midiCtrlButtonGroup,
			midiResolution: CVWidget.resolution,
			snapDistance: CVWidget.snapDistance,
			// special case: a classvar getter/setter can only be defined as a literal
			midiInputMapping: CVWidget.inputMapping
		));

		wmc.midiConnections ?? { wmc.midiConnections = () };
		wmc.midiConnections.m ?? {
			wmc.midiConnections.m = Ref(List[]);
		};
		wmc.midiConnections.m.value.add(nil);

		wmc.midiDisplay ?? { wmc.midiDisplay = () };
		wmc.midiDisplay.m ?? {
			wmc.midiDisplay.m = Ref(List[]);
		};
		wmc.midiDisplay.m.value.add((
			src: 'source...',
			chan: "chan",
			ctrl: "ctrl",
			learn: "L",
			toolTip: "Click and move hardware slider/knob to connect to"
		));
		wmc.midiConnectorNames ?? { wmc.midiConnectorNames = () };
		wmc.midiConnectorNames.m ?? {
			wmc.midiConnectorNames.m = Ref(List[]);
		};
		wmc.midiConnectorNames.m.value.add(name);
		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitMidiConnectors,
			prInitMidiConnectorNames,
			prInitMidiOptions,
			prInitMidiConnection,
			prInitMidiDisplay
		].do { |method|
			this.perform(method, wmc, widget.cv)
		}
	}

	// private: default controllers
	prInitMidiConnectors { |mc, cv|
		mc.midiConnectors.c ?? {
			mc.midiConnectors.c = SimpleController(mc.midiConnectors.m);
		};
		mc.midiConnectors.c.put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	prInitMidiOptions { |mc, cv|
		mc.midiOptions.c ?? {
			mc.midiOptions.c = SimpleController(mc.midiOptions.m);
		};
		mc.midiOptions.c.put(\default, { |changer, what ... moreArgs|
			// var index = mc.midiConnectors.m.value.indexOf(this);
			// do something...
		})
	}

	prInitMidiConnection { |mc, cv|
		mc.midiConnections.c ?? {
			mc.midiConnections.c = SimpleController(mc.midiConnections.m);
		};
		mc.midiConnections.c.put(\default, { |changer, what ... moreArgs|
			// do something...
		})
	}

	prInitMidiDisplay { |mc, cv|
		mc.midiDisplay.c ?? {
			mc.midiDisplay.c = SimpleController(mc.midiDisplay.m);
		};
		mc.midiDisplay.c.put(\default, { |changer, what ... moreArgs|
			// "midiDisplay.c.triggered".postln;
			// 	var index = mc.midiConnectors.m.value.indexOf(this);
			// 	// "midiDisplay.c - changer.value: %, moreArgs: %".format(changer.value, index).postln;
			// 	// ...
		})
	}

	prInitMidiConnectorNames { |mc, cv|
		mc.midiConnectorNames.c ?? {
			mc.midiConnectorNames.c = SimpleController(mc.midiConnectorNames.m);
		};
		mc.midiConnectorNames.c.put(\default, { |changer, what ... moreArgs|
			// 	"midiConnectorNames.c triggered:\n\t%\n\t%\n\t%".format(changer.value, what, moreArgs).postln;
		})
	}

	index {
		^widget.midiConnectors.indexOf(this);
	}

	name {
		^widget.wmc.midiConnectorNames.m.value[this.index]
	}

	name_ { |name|
		var index = this.index;
		var mc = widget.wmc;
		mc.midiConnectorNames.m.value[index] = name.asSymbol;
		mc.midiConnectorNames.m.changedPerformKeys(widget.syncKeys, index);
	}

	setMidiMode { |mode|
		var index = this.index;
		var mc = widget.wmc;
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};
		mc.midiOptions.m.value[index].midiMode = mode;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiMode {
		^widget.wmc.midiOptions.m.value[this.index].midiMode;
	}

	setMidiZero { |zeroval|
		var index = this.index;
		var mc = widget.wmc;
		zeroval = zeroval.asInteger;
		mc.midiOptions.m.value[index].midiZero = zeroval;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiZero {
		^widget.wmc.midiOptions.m.value[this.index].midiZero;
	}

	setMidiSnapDistance { |snapDistance|
		var index = this.index;
		var mc = widget.wmc;
		snapDistance = snapDistance.asFloat;
		mc.midiOptions.m.value[index].snapDistance = snapDistance;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiSnapDistance {
		^widget.wmc.midiOptions.m.value[this.index].snapDistance;
	}

	setMidiCtrlButtonGroup { |numButtons|
		var index = this.index;
		var mc = widget.wmc;
		if (numButtons.notNil and:{ numButtons.isInteger.not }) {
			Error("setMidiCtrlButtonGroup: 'numButtons' must either be an Integer or nil!").throw;
		};
		mc.midiOptions.m.value[index].ctrlButtonGroup = numButtons;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiCtrlButtonGroup {
		^widget.wmc.midiOptions.m.value[this.index].ctrlButtonGroup;
	}

	setMidiResolution { |resolution|
		var index = this.index;
		var mc = widget.wmc;
		mc.midiOptions.m.value[index].midiResolution = resolution;
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiResolution {
		^widget.wmc.midiOptions.m.value[this.index].midiResolution;
	}

	setMidiInputMapping { |mapping, curve = 0, env(Env([0, 1], [1]))|
		var index = this.index;
		var mc = widget.wmc;
		mapping = mapping.asSymbol;
		[\linlin, \linexp, \explin, \expexp, \lincurve, \linbicurve, \linenv].indexOf(mapping) ?? {
			"arg 'mapping' must be one of \\linlin, \\linexp, \\explin, \\expexp, \\lincurve, \\linbicurve or \\linenv".error;
			^this
		};
		// special care needs to be taken to NOT set CVWidget.inputMapping
		// not working, would set CVWidget.inputMapping too:
		// mc..midiOptions.m.value[index].midiInputMapping.mapping = mapping;
		mc.midiOptions.m.value[index].midiInputMapping_((mapping: mapping));
		case
		{ mapping === \lincurve or: { mapping === \linbicurve }} {
			mc.midiOptions.m.value[index].midiInputMapping.curve = curve;
		}
		{ mapping === \linenv } {
			mc.midiOptions.m.value[index].midiInputMapping.env = env;
		};
		mc.midiOptions.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiInputMapping {
		^widget.wmc.midiOptions.m.value[this.index].midiInputMapping;
	}

	setMidiTemplate { |argTemplate|
		var index = this.index;
		var mc = widget.wmc;
		mc.midiDisplay.m.value[index].template = argTemplate;
		mc.midiDisplay.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiTemplate {
		^widget.wmc.oscDisplay.m.value[this.index].template;
	}

	setMidiDispatcher { |dispatcher|
		var index = this.index;
		var mc = widget.wmc;
		mc.midiDisplay.m.value[index].dispatcher = dispatcher;
		mc.midiDisplay.m.changedPerformKeys(widget.syncKeys, index);
	}

	getMidiDispatcher {
		^widget.wmc.midiDisplay.m.value[this.index].dispatcher;
	}

	midiConnect { |num, chan, srcID, argTemplate, dispatcher|
		var index = this.index;
		var mc = widget.wmc;
		mc.midiConnections.m.value[index] = this.prMIDIFunc(index, num, chan, srcID, argTemplate, dispatcher);
		mc.midiConnections.m.changedPerformKeys(widget.syncKeys, index);
		mc.midiDisplay.m.value[index].learn = "X";
		mc.midiDisplay.m.value[index].toolTip = "Click to disconnect";
		mc.midiConnections.m.value[index].srcID !? {
			mc.midiDisplay.m.value[index].src = mc.midiConnections.m.value[index].srcID
		};
		mc.midiConnections.m.value[index].chan !? {
			mc.midiDisplay.m.value[index].chan = mc.midiConnections.m.value[index].chan
		};
		mc.midiConnections.m.value[index].msgNum !? {
			mc.midiDisplay.m.value[index].ctrl = mc.midiConnections.m.value[index].msgNum
		};
		mc.midiConnections.m.value[index].argTemplate !? {
			mc.midiDisplay.m.value[index].template = mc.midiConnections.m.value[index].argTemplate
		};
		mc.midiDisplay.m.value[index].dispatcher = mc.midiConnections.m.value[index].dispatcher;
		mc.midiDisplay.m.changedPerformKeys(widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? { this.midiDisconnect }
		})
	}

	midiDisconnect {
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);
		mc.midiConnections.m.value[index].clear;
		mc.midiConnections.m.value[index] = nil;
		mc.midiConnections.m.changedPerformKeys(widget.syncKeys, index);
		mc.midiDisplay.m.value[index].src = 'source...';
		mc.midiDisplay.m.value[index].chan = "chan";
		mc.midiDisplay.m.value[index].ctrl = "ctrl";
		mc.midiDisplay.m.value[index].template = nil;
		mc.midiDisplay.m.value[index].dispatcher = nil;
		mc.midiDisplay.m.value[index].toolTip = "Click and move hardware slider/knob to connect to";
		mc.midiDisplay.m.value[index].learn = "L";
		mc.midiDisplay.m.changedPerformKeys(widget.syncKeys, index);
		CmdPeriod.remove({
			this.widget !? { this.midiDisconnect }
		})
	}

	prMIDIFunc { |index, cc, ch, src, t, d|
		var snapDistance, inputMapping, input;
		var cv = widget.cv, learn;
		var makeFunc = { |argSrc, argChan, argNum, argTempl, argDispatcher|
			if (widget.wmc.midiConnections.m.value[index].isNil or: {
				widget.wmc.midiConnections.m.value[index].func.isNil
			}) {
				widget.wmc.midiConnections.m.value[index] = MIDIFunc.cc(
					ccAction,
					ccNum: argNum !? { argNum.asInteger },
					chan: argChan !? { argChan.asInteger },
					srcID: argSrc !? { argSrc.asInteger },
					argTemplate: argTempl,
					dispatcher: argDispatcher
				)
			};
			widget.wmc.midiConnections.m.value[index]
		};

		var ccAction = { |val, num, chan, src|
			// MIDI learn
			// we must infer the connections parameters here
			inputMapping = this.getMidiInputMapping;
			this.getMidiMode.switch(
				//  0-127
				0, {
					input = val/127;
					snapDistance = this.getMidiSnapDistance;
					if ((snapDistance <= 0).or(
						input < (cv.input + (snapDistance/2)) and: {
							input > (cv.input - (snapDistance/2))
					})) {
						case
						{ inputMapping.mapping === \lincurve } {
							if (inputMapping.curve != 0 and: { snapDistance > 0 }) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(input.lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
						}
						{ inputMapping.mapping === \linbicurve } {
							if (inputMapping.curve != 0 and: { snapDistance > 0 }) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(input.linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
						}
						{ inputMapping.mapping === \linenv } {
							if (snapDistance > 0) {
								this.setMidiSnapDistance(0)
							};
							cv.input_(input.linenv(env: inputMapping.env))
						}
						{ inputMapping.mapping === \explin } {
							if (snapDistance > 0) {
								this.setMidiSnapDistance(0)
							};
							cv.input_((input+1).explin(1, 2, 0, 1))
						}
						{ inputMapping.mapping === \expexp or: {inputMapping.mapping === \linexp }} {
							if (widget.getSpec.hasZeroCrossing and: { this.getMidiInputMapping !== \linlin}) {
								this.setMidiInputMapping(\linlin);
								cv.input_(input.linlin(0, 1, 0, 1))
							} {
								if (snapDistance > 0) {
									this.setMidiSnapDistance(0)
								};
								cv.value_((input+1).perform(inputMapping.mapping, 1, 2, widget.getSpec.minval, widget.getSpec.maxval))
							}
						}
						{
							cv.input_(input);
						}
					};
					// avoid jumps if another endless connection exists
					accum[widget] = cv.input;
				},
				// endless mode
				1, {
					// "midiMode is endless".postln;
					// we can't use cv.input (range: 0-1) in curved ramps or enveloped ramps
					// accumulation must happen within a linear ramp
					accum[widget] = accum[widget] + (val-this.getMidiZero/127*this.getMidiResolution);

					// accumulation is by default not limited like cv.input
					case
					{ accum[widget] < 0 } { accum[widget] = 0 }
					{ accum[widget] > 1 } { accum[widget] = 1 };

					case
					{ inputMapping.mapping === \lincurve } {
						cv.input_(accum[widget].lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
					}
					{ inputMapping.mapping === \linbicurve } {
						cv.input_(accum[widget].linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
					}
					{ inputMapping.mapping === \linenv } {
						cv.input_(accum[widget].linenv(env: inputMapping.env))
					}
					{ inputMapping.mapping === \explin } {
						cv.input_((accum[widget]+1).explin(1, 2, 0, 1))
					}
					{ inputMapping.mapping === \expexp or: { inputMapping.mapping === \linexp }} {
						if (widget.getSpec.hasZeroCrossing) {
							this.setMidiInputMapping(\linlin);
							cv.input_(accum[widget])
						} {
							cv.value_((accum[widget]+1).perform(inputMapping.mapping, 1, 2, widget.getSpec.minval, widget.getSpec.maxval))
						}
					}
					{ cv.input_(accum[widget]) };
				}
			);
		};

		learn = cc.isNil and: { ch.isNil  and: {src.isNil }};
		if (learn) {
			"MIDIFunc at widget.wmc.midiConnections.m.value[%] should learn".format(index).inform;
			makeFunc.().learnSync(widget, index);
		} {
			"MIDIFunc at widget.wmc.midiConnections.m.value[%] was set to src: %, channel: %, number: %".format(
				index, src, ch, cc
			).inform;
			makeFunc.(src, ch, cc, t, d);
		};
		accum[widget] = cv.input;
		^widget.wmc.midiConnections.m.value[index]
	}

	remove { |forceAll = false|
		var mc = widget.wmc;
		var index = mc.midiConnectors.m.value.indexOf(this);

		if (mc.midiConnectors.m.value.size > 1 or: { forceAll }) {
			this.midiDisconnect;
			// allMidiFuncs[widget][index].free;
			// allMidiFuncs[widget].removeAt(index);
			[
				mc.midiOptions.m.value,
				mc.midiConnections.m.value,
				mc.midiDisplay.m.value,
				mc.midiConnectorNames.m.value
			].do(_.removeAt(index));
			mc.midiConnectors.m.value.remove(this);
			mc.midiConnectors.m.changedPerformKeys(widget.syncKeys, index);
			// set editor elements (and other custom elements depending
			// on mc.midiConnectors.m.value) to suitable connector
			onConnectorRemove.value(widget, index);
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [widget.name.cs, this.name] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}

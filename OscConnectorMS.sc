OscConnectorMS : AbstractConnector {
	classvar <accum;
	classvar <onConnectorRemove;

	var <slot;

	*initClass {
		accum = ();
	}

	*new { |widget, name, slot|
		if (widget.class === Symbol or: { widget.isString }) {
			widget = CVWidget.all[widget.asSymbol]
		};
		if (widget.isNil or: {
			widget.class !== CVWidgetMS
		}) {
			Error("An OscConnectorMS can only be created for an existing CVWidget").throw;
		};
		if (slot.isNil or: { slot.isNumber.not }) {
			"Please provide a numeric slot for a new OscConnectorMS!".error;
			^nil
		};
		^super.newCopyArgs(widget, slot.asInteger).init(name);
	}

	*onConnectorRemove_ { |func|
		onConnectorRemove = onConnectorRemove.addFunc(func)
	}

	init { |name|
		this.widget.numOscConnectors[this.slot] = this.widget.numOscConnectors[this.slot] + 1;
		name ?? {
			name = "OSC Connection %".format(this.widget.numOscConnectors[this.slot]).asSymbol;
		};

		this.initModels(this.widget.wmc, name);

		this.widget.wmc.oscConnectors.m[this.slot].value_(
			this.widget.wmc.oscConnectors.m[this.slot].value.add(this)
		).changedPerformKeys(this.widget.syncKeys);
	}

	initModels { |wmc, name|
		var size = this.widget.size;
		// Is it wise to keep separate slots for each model?
		// or should all slots be within one model?
		wmc.oscConnections ?? { wmc.oscConnections = () };
		wmc.oscConnections.m ?? {
			wmc.oscConnections.m = List.newClear(size);
		};
		wmc.oscConnections.m[this.slot] ?? {
			wmc.oscConnections.m[this.slot] = Ref(List[])
		};
		wmc.oscConnections.m[this.slot].value.add(nil);

		wmc.oscDisplay ?? { wmc.oscDisplay = () };
		wmc.oscDisplay.m ?? {
			wmc.oscDisplay.m = List.newClear(size);
		};
		wmc.oscDisplay.m[this.slot] ?? {
			wmc.oscDisplay.m[this.slot] = Ref(List[])
		};
		wmc.oscDisplay.m[this.slot].value.add((
			nameField: '/path/to/cmd',
			index: 1,
			connectState: ["learn", Color.yellow, Color.green(0.5)],
			connectEnabled: true, // default, if no command is given
			learn: true, // default, no command given
			numOscSlots: 1,
			alwaysPositive: 0.1,
			slotToolTip: "Select the the CVWidgetMS's '%' slot (widget has % slots)."
		));

		wmc.oscOptions ?? { wmc.oscOptions = () };
		wmc.oscOptions.m ?? {
			wmc.oscOptions.m = List.newClear(size)
		};
		wmc.oscOptions.m[this.slot] ?? {
			wmc.oscOptions.m[this.slot] = Ref(List[])
		};
		wmc.oscOptions.m[this.slot].value.add((
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
			wmc.oscConnectorNames.m = List.newClear(size);
		};
		wmc.oscConnectorNames.m[this.slot] ?? {
			wmc.oscConnectorNames.m[this.slot] = Ref(List[])
		};
		wmc.oscConnectorNames.m[this.slot].value.add(name);

		wmc.oscInputConstrainters ?? {
			wmc.oscInputConstrainters = List.newClear(size);
		};
		wmc.oscInputConstrainters[this.slot] ?? {
			wmc.oscInputConstrainters[this.slot] = List[]
		};
		wmc.oscInputConstrainters[this.slot].add((
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
			this.perform(method, wmc, this.widget.cv)
		}
	}

	prInitOscConnectors { |mc, cv|
		mc.oscConnectors.c ?? {
			mc.oscConnectors.c = List.newClear(this.widget.size)
		};
		mc.oscConnectors.c[this.slot] = SimpleController(mc.oscConnectors.m[this.slot]);
		mc.oscConnectors.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}


	prInitOscConnections { |mc, cv|
		mc.oscConnections.c ?? {
			mc.oscConnections.c = List.newClear(this.widget.size)
		};
		mc.oscConnections.c[this.slot] = SimpleController(mc.oscConnections.m[this.slot]);
		mc.oscConnections.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	prInitOscDisplay { |mc, cv|
		mc.oscDisplay.c ?? {
			mc.oscDisplay.c = List.newClear(this.widget.size)
		};
		mc.oscDisplay.c[this.slot] = SimpleController(mc.oscDisplay.m[this.slot]);
		mc.oscDisplay.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	prInitOscOptions { |mc, cv|
		mc.oscOptions.c ?? {
			mc.oscOptions.c = List.newClear(this.widget.size)
		};
		mc.oscOptions.c[this.slot] = SimpleController(mc.oscOptions.m[this.slot]);
		mc.oscOptions.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	prInitOscConnectorNames { |mc, cv|
		mc.oscConnectorNames.c ?? {
			mc.oscConnectorNames.c = List.newClear(this.widget.size)
		};
		mc.oscConnectorNames.c[this.slot] = SimpleController(mc.oscConnectorNames.m[this.slot]);
		mc.oscConnectorNames.c[this.slot].put(\default, { |changer, what ... moreArgs|
			// blablabla, do something...
		})
	}

	index {
		^this.widget.oscConnectors[this.slot].value.indexOf(this)
	}

	name {
		^this.widget.wmc.oscConnectorNames.m[this.slot].value[this.index]
	}

	name_ { |name|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscConnectorNames.m[this.slot].value[index] = name.asSymbol;
		mc.oscConnectorNames.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
	}

	setOscOption { |option, value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscOptions.m[this.slot].value[index][option] = value;
		mc.oscOptions.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscOption { |option|
		var index = this.index;
		^this.widget.wmc.oscOptions.m[this.slot].value[index][option]
	}

	setOscInputConstraints { |constraintsPair|
		var index = this.index;
		var mc = this.widget.wmc;
		var lo, hi;

		if (constraintsPair.class === Point) {
			lo = constraintsPair.x;
			hi = constraintsPair.y;
		} {
			#lo, hi = constraintsPair;
		};
		mc.oscInputConstrainters[this.slot][index].lo.value_(lo);
		mc.oscInputConstrainters[this.slot][index].hi.value_(hi);

		this.setOscOption(\oscInputRange, [lo, hi])
	}

	setOscInputMapping { |mapping, curve(0), env(Env([0, 1], [1]))|
		var index = this.index;
		var mc = this.widget.wmc;
		mapping = mapping.asSymbol;
		[\linlin, \linexp, \explin, \expexp, \lincurve, \linbicurve, \linenv].indexOf(mapping) ?? {
			"arg 'mapping' must be one of \\linlin, \\linexp, \\explin, \\expexp, \\lincurve, \\linbicurve or \\linenv".error;
			^this
		};
		// special care needs to be taken to NOT set CVWidget.inputMapping
		// not working, would set CVWidget.inputMapping too:
		// mc..oscOptions.m.value[index].oscInputMapping.mapping = mapping;
		mc.oscOptions.m[this.slot].value[index].oscInputMapping_((mapping: mapping));
		case
		{ mapping === \lincurve or: { mapping === \linbicurve }} {
			mc.oscOptions.m[this.slot].value[index].oscInputMapping.curve = curve;
		}
		{ mapping === \linenv } {
			mc.oscOptions.m[this.slot].value[index].oscInputMapping.env = env;
		};
		mc.oscOptions.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
	}

	setOscDisplay { |displayValueName, value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscDisplay.m[this.slot].value[index][displayValueName] = value;
		mc.oscDisplay.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscDisplay { |displayValueName|
		var index = this.index;
		^this.widget.wmc.oscDisplay.m[this.slot].value[index][displayValueName]
	}

	getSlotToolTip {
		^this.getOscDisplay(\slotToolTip)
	}

	setOSCFuncEnabled { |boolEnabled|
		var index = this.index;
		var m = this.widget.wmc.oscConnections.m[this.slot];
		if (m.value[index].isNil) {
			"connector at index % is currently not connected.".format(index).inform
		} {
			if (boolEnabled) { m.value[index].enable } { m.value[index].disable };
			m.changedPerformKeys(this.widget.syncKeys, index);
		}
	}

	getOSCFuncEnabled {
		if (this.widget.wmc.oscConnections.m[this.slot].value[this.index].notNil) {
			^this.widget.wmc.oscConnections.m[this.slot].value[this.index].enabled
		} { ^true }
	}

	oscConnect { |addr, cmdPath, oscMsgIndex(1), recvPort, argTemplate, dispatcher, matching(false)|
		var index = this.index;
		var mc = this.widget.wmc;
		if (addr.notNil and: { addr.class !== NetAddr }) {
			"addr is not a valid NetAddr".error;
			^nil
		};
		mc.oscConnections.m[this.slot].value[index] = this.prOSCFunc(addr, cmdPath, oscMsgIndex, recvPort, argTemplate, dispatcher, matching).postln;
		mc.oscConnections.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
		addr !? {
			if (addr.ip != "0.0.0.0" and: { CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol].isNil }) {
				CVWidget.wmc.oscAddrAndCmds.m.value.put(addr.ip.asSymbol, ());
			};
			if (CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol].notNil and: { addr.port.notNil }) {
				if (CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol][addr.port.asSymbol].isNil) {
					CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol].put(addr.port.asSymbol, (cmdPath.asSymbol : 1))
				} {
					CVWidget.wmc.oscAddrAndCmds.m.value[addr.ip.asSymbol][addr.port.asSymbol].put(cmdPath.asSymbol, 1)
				}
			};
			CVWidget.wmc.oscAddrAndCmds.m.changedPerformKeys(CVWidget.syncKeys);
			mc.oscConnections.m[this.slot].value[index] !? {
				mc.oscDisplay.m[this.slot].value[index].ipField = mc.oscConnections.m[this.slot].value[index].srcID.ip.asSymbol;
				mc.oscDisplay.m[this.slot].value[index].portField = mc.oscConnections.m[this.slot].value[index].srcID.port;
			};
		};
		mc.oscDisplay.m[this.slot].value[index].nameField = mc.oscConnections.m[this.slot].value[index].path;
		mc.oscDisplay.m[this.slot].value[index].template = mc.oscConnections.m[this.slot].value[index].argTemplate.cs;
		mc.oscDisplay.m[this.slot].value[index].dispatcher = mc.oscConnections.m[this.slot].value[index].dispatcher;
		mc.oscDisplay.m[this.slot].value[index].connectState = ["disconnect", Color.white, Color.red];
		// mc.oscDisplay.m[this.slot].value[index].connectorButVal = 1;
		// mc.oscDisplay.m[this.slot].value[index].connect = "disconnect";
		mc.oscDisplay.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? { this.oscDisconnect }
		})
	}

	oscDisconnect {
		var index = this.index;
		var mc = this.widget.wmc;
		// "free % at index %".format(mc.oscConnections.m.value[index], index).postln;
		mc.oscConnections.m[this.slot].value[index].free;
		mc.oscConnections.m[this.slot].value[index] = nil;
		mc.oscConnections.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
		// mc.oscDisplay.m[this.slot].value[index].ipField = nil;
		// mc.oscDisplay.m[this.slot].value[index].portField = nil;
		// mc.oscDisplay.m[this.slot].value[index].template = nil;
		mc.oscDisplay.m[this.slot].value[index].dispatcher = nil;
		mc.oscDisplay.m[this.slot].value[index].learn = false;
		// mc.oscDisplay.m.value[index].connectorButVal = 0;
		// mc.oscDisplay.m.value[index].connect = "connect";
		mc.oscDisplay.m[this.slot].value[index].connectState = ["connect", Color.white, Color.blue];
		mc.oscDisplay.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
		CmdPeriod.remove({
			this.widget !? { this.oscDisconnect }
		})
	}

	prOSCFuncAction { |mid|
		var input, inputRaw, corrDiff, cv = this.widget.cv, constraints, inputMapping, argValues;
		var snapDistance, constraintsRange;
		// multichannel-specific: cv.value/cv.input will be arrays
		var arrInput, arrValue;

		^{ |msg, time, addr, port|
			input = inputRaw = msg[mid ?? { this.getOscMsgIndex }];
			if (input <= 0 and: { input.abs > this.getOscInputAlwaysPositive }) {
				this.setOscInputAlwaysPositive(input.abs + 0.1);
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

			inputMapping = this.getOscInputMapping;
			argValues = [
				inputMapping.mapping,
				constraints[0] + this.getOscInputAlwaysPositive,
				constraints[1] + this.getOscInputAlwaysPositive,
			];

			if (inputMapping.mapping === \linbicurve) {
				argValues = argValues.add(nil) // inCenter, internally computed
			};

			// minval, maxval can be aarays of diffent sizes (or one is an array and the other a number)
			// hence we have to sanitize when using either
			argValues = argValues.addAll([
				this.widget.getSpec.minval.asArray.wrapAt(this.slot),
				this.widget.getSpec.maxval.asArray.wrapAt(this.slot)
			]);

			if (inputMapping.mapping === \linbicurve) {
				argValues = argValues.add(nil) // outCenter, internally computed
			};

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
				// [input, input+this.getOscInputAlwaysPositive, input/constraintsRange, (input+this.getOscInputAlwaysPositive)/constraintsRange].postln;
				if (constraintsRange == 0) { input = 0 } {
					input = input+this.getOscInputAlwaysPositive
				};
				// "input: %\ninputRaw: %\ncv.input: %\ncv.spec.minval: %\n".format(input, inputRaw, cv.value, cv.spec.minval).postln;
				if ((snapDistance <= 0).or(
					inputRaw < (cv.input[this.slot] + (snapDistance)) and: {
						inputRaw > (cv.input[this.slot] - (snapDistance))
					}
				)) {
					case
					{ inputMapping.mapping === \lincurve or: { inputMapping.mapping === \linbicurve }} {
						if (inputMapping.curve != 0 and: { snapDistance > 0 }) {
							this.setOscSnapDistance(0)
						}
					}
					{ inputMapping.mapping === \linenv } {
						if (inputMapping.env != Env([0, 1], [1]) and: { snapDistance > 0 }) {
							this.setOscSnapDistance(0)
						}
					}
					{ inputMapping.mapping === \linexp } {
						if (cv.spec.minval.asArray.wrapAt(this.slot) <= 0 or: { cv.spec.maxval.asArray.wrapAt(this.slot) <= 0 }) {
							this.setOscInputMapping(\linlin);
						} {
							if (snapDistance > 0) {
								this.setOscSnapDistance(0)
							}
						}
					}
					{ inputMapping.mapping === \explin } {
						if (snapDistance > 0) {
							this.setOscSnapDistance(0)
						};
						arrInput = cv.input;
						arrInput[this.slot] = (input+1).explin(1, 2, 0, 1);
						cv.input_(arrInput)
					};
					// "input: %".format(input).warn;
					arrValue = cv.value;
					arrValue[this.slot] = input.perform(*argValues);
					cv.value_(arrValue);
					// "cv.value: %\n".format(cv.value).postln;
				};
				// TODO: what is accum???
				accum[this.widget] = cv.input;
			} {
				accum[this.widget][this.slot] = accum[this.widget][this.slot] + (input / constraintsRange / 32 * this.getOscResolution);

				case
				{ accum[this.widget][this.slot] < 0 } { accum[this.widget][this.slot] = 0 }
				{ accum[this.widget][this.slot] > 1 } { accum[this.widget][this.slot] = 1 };

				// [input, accum[this.widget], inputMapping, this.getOscResolution].postln;

				case
				{ inputMapping.mapping === \lincurve } {
					cv.input_(
						cv.input[..this.slot-1] ++
						accum[this.widget][this.slot].lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve) ++
						cv.input[this.slot+1..]
					)
				}
				{ inputMapping.mapping === \linbicurve } {
					cv.input_(
						cv.input[..this.slot-1] ++
						accum[this.widget][this.slot].linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve) ++
						cv.input[this.slot+1..]
					)
				}
				{ inputMapping.mapping === \linenv } {
					cv.input_(
						cv.input[..this.slot-1] ++
						accum[this.widget][this.slot].linenv(env: inputMapping.env) ++
						cv.input[this.slot+1..]
					)
				}
				{ inputMapping.mapping === \explin } {
					cv.input_(
						cv.input[..this.slot-1] ++
						(accum[this.widget][this.slot]+1).explin(1, 2, 0, 1) ++
						cv.input[this.slot+1..]
					)
				}
				{ inputMapping.mapping === \expexp or: { inputMapping.mapping === \linexp }} {
					if (this.widget.getSpec.hasZeroCrossing) {
						this.setOscInputMapping(\linlin);
						cv.input_(
							cv.input[..this.slot-1] ++
							accum[this.widget][this.slot] ++
							cv.input[this.slot+1..]
						)
					} {
						cv.value_(
							cv.input[..this.slot-1] ++
							(accum[this.widget]+1).perform(
								inputMapping.mapping, 1, 2,
								this.widget.getSpec.minval.asArray.wrapAt(this.slot),
								this.widget.getSpec.maxval.asArray.wrapAt(this.slot)
							) ++
							cv.input[this.slot+1..]
						)
					}
				}
				{ cv.input_(accum[this.widget]) }
			}
		}
	}

	prOSCFunc { |a, c, mid, r, t, d, m|
		// [a, c, mid, r, t, d, m].postln;
		accum[this.widget] ?? {
			accum[this.widget] = nil ! this.widget.size
		};
		accum[this.widget][this.slot] = this.widget.cv.input[this.slot];
		^if (m) {
			^OSCFunc.newMatching(this.prOSCFuncAction(mid), c, a, r, t)
		} {
			^OSCFunc(this.prOSCFuncAction(mid), c, a, r, t, d).postln
		}
	}

	remove { |forceAll = false|
		var mc = this.widget.wmc;
		// var wmc = CVWidget.wmc;
		var index = this.index;

		if (mc.oscConnectors.m[this.slot].value.size > 1 or: { forceAll }) {
			this.oscDisconnect;
			// allOscFuncs??
			[
				mc.oscDisplay.m[this.slot].value,
				mc.oscConnections.m[this.slot].value,
				mc.oscConnectorNames.m[this.slot].value,
				mc.oscOptions.m[this.slot].value
			].do(_.removeAt(index));
			mc.oscConnectors.m[this.slot].value.remove(this);
			mc.oscConnectors.m[this.slot].changedPerformKeys(this.widget.syncKeys, index);
			onConnectorRemove.value(this.widget, index);
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [this.widget.name.cs, this.name, this.slot] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}
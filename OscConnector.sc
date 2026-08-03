OscConnector : AbstractConnector {
	classvar <accum;
	classvar <onConnectorRemove;
	// var <alwaysPositive = 0.1;

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
		this.widget.numOscConnectors = this.widget.numOscConnectors + 1;
		name ?? {
			name = "OSC Connection %".format(this.widget.numOscConnectors).asSymbol;
		};

		this.initModels(this.widget.wmc, name);

		this.widget.wmc.oscConnectors.m.value_(
			this.widget.wmc.oscConnectors.m.value.add(this)
		).changedPerformKeys(this.widget.syncKeys);
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
			nameField: '/path/to/cmd',
			index: 1,
			connectState: ["learn", Color.yellow, Color.green(0.5)],
			connectEnabled: true, // default, if no command is given
			learn: true, // default, no command given
			numOscSlots: 1,
			alwaysPositive: 0.1
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
			this.perform(method, wmc, this.widget.cv)
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
		^this.widget.oscConnectors.indexOf(this);
	}

	name {
		^this.widget.wmc.oscConnectorNames.m.value[this.index];
	}

	name_ { |name|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscConnectorNames.m.value[index] = name.asSymbol;
		mc.oscConnectorNames.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	setOscOption { |option, value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscOptions.m.value[index][option] = value;
		mc.oscOptions.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscOption { |option|
		var index = this.index;
		^this.widget.wmc.oscOptions.m.value[index][option]
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
		mc.oscInputConstrainters[index].lo.value_(lo);
		mc.oscInputConstrainters[index].hi.value_(hi);

		this.setOscOption(\oscInputRange, [lo, hi])
	}

	setOscInputMapping { |mapping, curve = 0, env(Env([0, 1], [1]))|
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
		mc.oscOptions.m.value[index].oscInputMapping_((mapping: mapping));
		case
		{ mapping === \lincurve or: { mapping === \linbicurve }} {
			mc.oscOptions.m.value[index].oscInputMapping.curve = curve;
		}
		{ mapping === \linenv } {
			mc.oscOptions.m.value[index].oscInputMapping.env = env;
		};
		mc.oscOptions.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	setOscDisplay { |displayValueName, value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscDisplay.m.value[index][displayValueName] = value;
		mc.oscDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscDisplay { |displayValueName|
		var index = this.index;
		^this.widget.wmc.oscDisplay.m.value[index][displayValueName]
	}

	setOSCFuncEnabled { |boolEnabled|
		var index = this.index;
		var m = this.widget.wmc.oscConnections.m;
		if (m.value[index].isNil) {
			"connector at index % is currently not connected.".format(index).inform
		} {
			if (boolEnabled) { m.value[index].enable } { m.value[index].disable };
			m.changedPerformKeys(this.widget.syncKeys, index);
		}
	}

	getOSCFuncEnabled {
		if (this.widget.wmc.oscConnections.m.value[this.index].notNil) {
			^this.widget.wmc.oscConnections.m.value[this.index].enabled
		} { ^true }
	}

	oscConnect { |addr, cmdPath, oscMsgIndex(1), recvPort, argTemplate, dispatcher, matching(false)|
		var index = this.index;
		var mc = this.widget.wmc;
		if (addr.notNil and: { addr.class !== NetAddr }) {
			"addr is not a valid NetAddr".error;
			^nil
		};
		mc.oscConnections.m.value[index] = this.prOSCFunc(addr, cmdPath, oscMsgIndex, recvPort, argTemplate, dispatcher, matching);
		mc.oscConnections.m.changedPerformKeys(this.widget.syncKeys, index);
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
			// "mc.oscConnections.m.value[%]: %".format(index, mc.oscConnections.m.value[index]).postln;
			mc.oscConnections.m.value[index].srcID !? {
				mc.oscDisplay.m.value[index].ipField = mc.oscConnections.m.value[index].srcID.ip.asSymbol;
				mc.oscDisplay.m.value[index].portField = mc.oscConnections.m.value[index].srcID.port;
			};
		};
		mc.oscDisplay.m.value[index].nameField = mc.oscConnections.m.value[index].path;
		mc.oscDisplay.m.value[index].template = mc.oscConnections.m.value[index].argTemplate.cs;
		mc.oscDisplay.m.value[index].dispatcher = mc.oscConnections.m.value[index].dispatcher;
		mc.oscDisplay.m.value[index].connectState = ["disconnect", Color.white, Color.red];
		// mc.oscDisplay.m.value[index].connectorButVal = 1;
		// mc.oscDisplay.m.value[index].connect = "disconnect";
		mc.oscDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? { this.oscDisconnect }
		})
	}

	oscDisconnect {
		var index = this.index;
		var mc = this.widget.wmc;
		// "free % at index %".format(mc.oscConnections.m.value[index], index).postln;
		mc.oscConnections.m.value[index].free;
		mc.oscConnections.m.value[index] = nil;
		mc.oscConnections.m.changedPerformKeys(this.widget.syncKeys, index);
		// mc.oscDisplay.m.value[index].ipField = nil;
		// mc.oscDisplay.m.value[index].portField = nil;
		// mc.oscDisplay.m.value[index].template = nil;
		mc.oscDisplay.m.value[index].dispatcher = nil;
		mc.oscDisplay.m.value[index].learn = false;
		// mc.oscDisplay.m.value[index].connectorButVal = 0;
		// mc.oscDisplay.m.value[index].connect = "connect";
		mc.oscDisplay.m.value[index].connectState = ["connect", Color.white, Color.blue];
		mc.oscDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
		CmdPeriod.remove({
			this.widget !? { this.oscDisconnect }
		})
	}

	prOSCFuncAction { |mid|
		var input, inputRaw, corrDiff, cv = this.widget.cv, constraints, inputMapping, argValues;
		var snapDistance, constraintsRange;

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

			argValues = argValues.addAll([
				this.widget.getSpec.minval,
				this.widget.getSpec.maxval
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
					inputRaw < (cv.input + (snapDistance)) and: {
						inputRaw > (cv.input - (snapDistance))
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
						if (cv.spec.minval <= 0 or: { cv.spec.maxval <= 0 }) {
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
						cv.input_((input+1).explin(1, 2, 0, 1))
					};
					// "input: %".format(input).warn;
					cv.value_(input.perform(*argValues));
					// "cv.value: %\n".format(cv.value).postln;
				};
				accum[this.widget] = cv.input;
				// "accum (non-endless): %".format(accum).postln;
			} {
				accum[this.widget] = accum[this.widget] + (input / constraintsRange / 32 * this.getOscResolution);

				case
				{ accum[this.widget] < 0 } { accum[this.widget] = 0 }
				{ accum[this.widget] > 1 } { accum[this.widget] = 1 };
				// "accum (endless): %".format(accum).postln;

				// [input, accum[this.widget], inputMapping, this.getOscResolution].postln;

				case
				{ inputMapping.mapping === \lincurve } {
					cv.input_(accum[this.widget].lincurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
				}
				{ inputMapping.mapping === \linbicurve } {
					cv.input_(accum[this.widget].linbicurve(inMin: 0.0, inMax: 1.0, outMin: 0.0, outMax: 1.0, curve: inputMapping.curve))
				}
				{ inputMapping.mapping === \linenv } {
					cv.input_(accum[this.widget].linenv(env: inputMapping.env))
				}
				{ inputMapping.mapping === \explin } {
					cv.input_((accum[this.widget]+1).explin(1, 2, 0, 1))
				}
				{ inputMapping.mapping === \expexp or: { inputMapping.mapping === \linexp }} {
					if (this.widget.getSpec.hasZeroCrossing) {
						this.setOscInputMapping(\linlin);
						cv.input_(accum[this.widget])
					} {
						cv.value_((accum[this.widget]+1).perform(inputMapping.mapping, 1, 2, this.widget.getSpec.minval, this.widget.getSpec.maxval))
					}
				}
				{ cv.input_(accum[this.widget]) };
			}
		}
	}

	prOSCFunc { |a, c, mid, r, t, d, m|
		// [a, c, mid, r, t, d, m].postln;
		accum[this.widget] = this.widget.cv.input;
		^if (m) {
			^OSCFunc.newMatching(this.prOSCFuncAction(mid), c, a, r, t)
		} {
			^OSCFunc(this.prOSCFuncAction(mid), c, a, r, t, d)
		}
	}

	remove { |forceAll = false|
		var mc = this.widget.wmc;
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
			mc.oscConnectors.m.changedPerformKeys(this.widget.syncKeys, index);
			onConnectorRemove.value(this.widget, index);
		}
	}

	storeOn { |stream|
		stream << this.class.name << "(" <<* [this.widget.name.cs, this.name] << ")"
	}

	printOn { |stream|
		this.storeOn(stream)
	}
}

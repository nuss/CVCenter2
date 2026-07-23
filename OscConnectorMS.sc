OscConnectorMS : AbstractConnector {
	classvar cAnons = 0, <accum;
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

		this.widget.size.do { |i|
			this.widget.wmc.oscConnectors.m[i].value_(
				this.widget.wmc.oscConnectors.m[i].value.add(this)
			).changedPerformKeys(this.widget.syncKeys);
		}
	}

	initModels { |wmc, name|
		var size = this.widget.size;
		// Is it wise to keep separate slots for each model?
		// or should all slots be within one model?
		wmc.oscConnections ?? { wmc.oscConnections = () };
		wmc.oscConnections.m ?? {
			wmc.oscConnections.m = List[];
		};
		size.do { |i|
			wmc.oscConnections.m[i] ?? {
				wmc.oscConnections.m.add(Ref(List[]))
			};
			wmc.oscConnections.m[i].value.add(nil);
		};

		wmc.oscDisplay ?? { wmc.oscDisplay = () };
		wmc.oscDisplay.m ?? {
			wmc.oscDisplay.m = List[];
		};
		size.do { |i|
			wmc.oscDisplay.m[i] ?? {
				wmc.oscDisplay.m.add(Ref(List[]))
			};
			wmc.oscDisplay.m[i].value.add((
				nameField: '/path/to/cmd',
				index: 1,
				connectState: ["learn", Color.yellow, Color.green(0.5)],
				connectEnabled: true, // default, if no command is given
				learn: true, // default, no command given
				numOscSlots: 1,
				alwaysPositive: 0.1
			))
		};

		wmc.oscOptions ?? { wmc.oscOptions = () };
		wmc.oscOptions.m ?? {
			wmc.oscOptions.m = List[]
		};
		size.do { |i|
			wmc.oscOptions.m[i] ?? {
				wmc.oscOptions.m.add(Ref(List[]))
			};
			wmc.oscOptions.m[i].value.add((
				oscEndless: CVWidget.oscEndless,
				oscResolution: CVWidget.resolution,
				oscCalibration: CVWidget.oscCalibration,
				oscSnapDistance: CVWidget.snapDistance,
				oscInputMapping: CVWidget.inputMapping,
				oscInputRange: CVWidget.oscInputRange,
				oscMatching: CVWidget.oscMatching
			))
		};

		wmc.oscConnectorNames ?? { wmc.oscConnectorNames = () };
		wmc.oscConnectorNames.m ?? {
			wmc.oscConnectorNames.m = List[];
		};
		size.do { |i|
			wmc.oscConnectorNames.m[i] ?? {
				wmc.oscConnectorNames.m.add(Ref(List[]))
			};
			wmc.oscConnectorNames.m[i].value.add(name)
		};

		wmc.oscInputConstrainters ?? {
			wmc.oscInputConstrainters = List[];
		};
		size.do { |i|
			wmc.oscInputConstrainters[i] ?? {
				wmc.oscInputConstrainters.add(List[])
			};
			wmc.oscInputConstrainters[i].add((
				lo: CV([-inf, inf].asSpec, CVWidget.oscInputRange[0]),
				hi: CV([-inf, inf].asSpec, CVWidget.oscInputRange[1])
			));
		};

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
			mc.oscConnectors.c = List[]
		};
		this.widget.size.do { |i|
			mc.oscConnectors.c.add(SimpleController(mc.oscConnectors.m[i]));
			mc.oscConnectors.c[i].put(\default, { |changer, what ... moreArgs|
				// blablabla, do something...
			})
		}
	}

	prInitOscConnections { |mc, cv|
		mc.oscConnections.c ?? {
			mc.oscConnections.c = List[]
		};
		this.widget.size.do { |i|
			mc.oscConnections.c.add(SimpleController(mc.oscConnections.m[i]));
			mc.oscConnections.c[i].put(\default, { |changer, what ... moreArgs|
				// blablabla, do something...
			})
		}
	}

	prInitOscDisplay { |mc, cv|
		mc.oscDisplay.c ?? {
			mc.oscDisplay.c = List[]
		};
		this.widget.size.do { |i|
			mc.oscDisplay.c.add(SimpleController(mc.oscDisplay.m[i]));
			mc.oscDisplay.c[i].put(\default, { |changer, what ... moreArgs|
				// blablabla, do something...
			})
		}
	}

	prInitOscOptions { |mc, cv|
		mc.oscOptions.c ?? {
			mc.oscOptions.c = List[]
		};
		this.widget.size.do { |i|
			mc.oscOptions.c.add(SimpleController(mc.oscOptions.m[i]));
			mc.oscOptions.c[i].put(\default, { |changer, what ... moreArgs|
				// blablabla, do something...
			})
		}
	}

	prInitOscConnectorNames { |mc, cv|
		mc.oscConnectorNames.c ?? {
			mc.oscConnectorNames.c = List[]
		};
		this.widget.size.do { |i|
			mc.oscConnectorNames.c.add(SimpleController(mc.oscConnectorNames.m[i]));
			mc.oscConnectorNames.c[i].put(\default, { |changer, what ... moreArgs|
				// blablabla, do something...
			})
		}
	}

	index {
		^this.widget.oscConnectors.indexOf(this);
	}

	getName { |slot|
		if (slot.notNil) {
			^this.widget.wmc.oscConnectorNames.m[slot].value[this.index]
		} {
			// return array of names
			^this.widget.wmc.oscConnectorNames.m.collect(_.value[this.index])
		}
	}

	setName { |name, slot|
		var index = this.index;
		var mc = this.widget.wmc;
		if (slot.notNil) {
			mc.oscConnectorNames.m[slot].value[index] = name.asSymbol;
			mc.oscConnectorNames.m[slot].changedPerformKeys(this.widget.syncKeys, index);
		} {
			// change name for all slots at once
			mc.oscConnectorNames.m.do { |model|
				model.value[index] = name.asSymbol;
				model.changedPerformKeys(this.widget.syncKeys, index);
			}
		}
	}

	setOscOption { |option, value, slot|
		var index = this.index;
		var mc = this.widget.wmc;
		if (slot.notNil) {
			mc.oscOptions.m[slot].value[index][option] = value;
			mc.oscOptions.m[slot].changedPerformKeys(this.widget.syncKeys, index);
		} {
			mc.oscOptions.m.do { |model|
				model.value[index][option] = value;
				model.changedPerformKeys(this.widget, index);
			}
		}
	}

	getOscOption { |option, slot|
		var index = this.index;
		if (slot.notNil) {
			^this.widget.wmc.oscOptions.m[slot].value[index][option]
		} {
			^this.widget.wmc.oscOptions.m.collect { |model| model.value[index][option] }
		}
	}

	setOscEndless { |boolEndless, slot|
		this.setOscOption(\oscEndless, boolEndless, slot)
	}

	getOscEndless { |slot|
		^this.getOscOption(\oscEndless, slot)
	}

	setOscResolution { |resolution, slot|
		this.setOscOption(\oscResolution, resolution, slot)
	}

	getOscResolution { |slot|
		^this.getOscOption(\oscResolution, slot)
	}

	setOscSnapDistance { |distance, slot|
		this.setOscOption(\oscSnapDistance, distance, slot)
	}

	getOscSnapDistance { |slot|
		^this.getOscOption(\oscSnapDistance, slot)
	}

	setOscCalibration { |boolCalibration, slot|
		this.setOscOption(\oscCalibration, boolCalibration, slot)
	}

	getOscCalibration { |slot|
		^this.getOscOption(\oscCalibration, slot)
	}

	resetOscCalibration { |slot|
		this.setOscOption(\oscInputRange, CVWidget.oscInputRange, slot);
	}

	setOscInputConstraints { |constraintsPair, slot|
		var index = this.index;
		var mc = this.widget.wmc;
		var lo, hi;

		if (constraintsPair.class === Point) {
			lo = constraintsPair.x;
			hi = constraintsPair.y;
		} {
			#lo, hi = constraintsPair;
		};
		// is this right?
		// shouldn't it rather be mc.oscInputConstrainters[index][slot]?
		if (slot.notNil) {
			mc.oscInputConstrainters[slot][index].lo.value_(lo);
			mc.oscInputConstrainters[slot][index].hi.value_(hi);
		} {

		};

		mc.oscOptions.m.value[index].oscInputRange = [lo, hi];
		mc.oscOptions.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscInputConstraints {
		^this.widget.wmc.oscOptions.m.value[this.index].oscInputRange;
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

	getOscInputMapping {
		^this.widget.wmc.oscOptions.m.value[this.index].oscInputMapping;
	}

	setOscCmdName { |cmdPath|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscDisplay.m.value[index].nameField = cmdPath.asSymbol;
		mc.oscDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscCmdName {
		^this.widget.wmc.oscDisplay.m.value[this.index].nameField;
	}

	setOscInputAlwaysPositive { |value|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscDisplay.m.value[index].alwaysPositive = value;
		mc.oscDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscInputAlwaysPositive {
		^this.widget.wmc.oscDisplay.m.value[this.index].alwaysPositive;
	}

	setOscMsgIndex { |msgIndex|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscDisplay.m.value[index].index = msgIndex.asInteger;
		mc.oscDisplay.m.changedPerformKeys(this.widget.syncKeys, index)
	}

	getOscMsgIndex {
		^this.widget.wmc.oscDisplay.m.value[this.index].index;
	}

	setOscMatching { |boolMatching|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscOptions.m.value[index].oscMatching = boolMatching;
		mc.oscOptions.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscMatching {
		^this.widget.wmc.oscOptions.m.value[this.index].oscMatching;
	}

	setOscTemplate { |argTemplate|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscDisplay.m.value[index].oscTemplate = argTemplate.cs;
		mc.oscDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscTemplate {
		^this.widget.wmc.oscDisplay.m.value[this.index].oscTemplate.interpret;
	}

	setOscDispatcher { |dispatcher|
		var index = this.index;
		var mc = this.widget.wmc;
		mc.oscDisplay.m.value[index].dispatcher = dispatcher;
		mc.oscDisplay.m.changedPerformKeys(this.widget.syncKeys, index);
	}

	getOscDispatcher {
		^this.widget.wmc.oscDisplay.m.value[this.index].dispatcher;
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

}
OscConnection {
	classvar cAnons = 0;
	var <widget, <>name;
	// var <mc; // models and controllers

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("An OscConnection can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget, name).init;
	}

	init {
		this.name ?? {
			widget.numOscConnections = widget.numOscConnections + 1;
			this.name_("OSC Connection %".format(widget.numOscConnections).asSymbol);
		};
		// add to the widget's oscConnection and automatically update GUI
		widget.oscConnections.add(this).changed(\value);
		this.initModels(widget.wmc);
	}

	initModels { |wmc|
		wmc.oscCalibration ?? { wmc.oscCalibration = () };
		wmc.oscCalibration.model ?? {
			wmc.oscCalibration.model = List[];
		};
		wmc.oscCalibration.model.add(Ref(CVWidget.oscCalibration));

		wmc.oscInputRange ?? { wmc.oscInputRange = () };
		wmc.oscInputRange.model ?? {
			wmc.oscInputRange.model = List[];
		};
		wmc.oscInputRange.model.add(Ref([0.0001, 0.0001]));

		wmc.oscConnections ?? { wmc.oscConnections = () };
		wmc.oscConnections.model ?? {
			wmc.oscConnections.model = List[];
		};
		wmc.oscConnections.model.add(Ref(false));

		wmc.oscDisplay ?? { wmc.oscDisplay = () };
		wmc.oscDisplay.model ?? {
			wmc.oscDisplay.model = List[];
		};
		wmc.oscDisplay.model.add(Ref((
			ipField: nil,
			portField: nil,
			nameField: "/my/cmd/name",
			index: 1,
			connectorButVal: 0,
			editEnabled: true
		)));

		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitOscCalibration,
			prInitOscInputRange,
			prInitOscConnect,
			prInitOscDisplay
		].do { |method|
			this.perform(method, wmc, widget.cv)
		}
	}

	prInitOscCalibration { |mc, cv|
		mc.oscCalibration.controller ?? {
			mc.oscCalibration.controller = SimpleController(mc.oscCalibration.model)
		};
		mc.oscCalibration.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscInputRange { |mc, cv|
		mc.oscInputRange.controller ?? {
			mc.oscInputRange.controller = SimpleController(mc.oscInputRange.model)
		};
		mc.oscInputRange.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscConnect { |mc, cv|
		mc.oscConnections.controller ?? {
			mc.oscConnections.controller = SimpleController(mc.oscConnections.model)
		};
		mc.oscConnections.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	prInitOscDisplay { |mc, cv|
		mc.oscDisplay.controller ?? {
			mc.oscDisplay.controller = SimpleController(mc.oscDisplay.model);
		};
		mc.oscDisplay.controller.put(\default, { |changer, what, moreArgs|
			// do something with changer.value
		})
	}

	remove {
		// remove views, OSCdefs...
	}

	oscConnect {}
	oscDisconnect {}
}

MidiConnection {
	classvar cAnons = 0;
	var <widget, <>name;
	// var <mc; // models and controllers
	var midiFunc;

	*new { |widget, name|
		if (widget.isNil or: {
			widget.isKindOf(CVWidget).not
		}) {
			Error("A MidiConnection can only be created for an existing CVWidget").throw;
		};
		^super.newCopyArgs(widget, name).init;
	}

	init {
		this.name ?? {
			widget.numMidiConnections = widget.numMidiConnections + 1;
			this.name_("MIDI Connection %".format(widget.numMidiConnections).asSymbol);
		};
		widget.midiConnections.add(this).changed(\value);
		this.initModels(widget.wmc);
	}

	initModels { |wmc|
		wmc.midiOptions ?? { wmc.midiOptions = () };
		wmc.midiOptions.model ?? {
			wmc.midiOptions.model = List[];
		};
		wmc.midiOptions.model.add(Ref((
			midiMode: CVWidget.midiMode,
			midiMean: CVWidget.midiMean,
			ctrlButtonBank: CVWidget.ctrlButtonBank,
			midiResolution: CVWidget.midiResolution,
			softWithin: CVWidget.softWithin
		)));

		wmc.midiConnections ?? { wmc.midiConnections = () };
		wmc.midiConnections.model ?? {
			wmc.midiConnections.model = List[];
		};
		wmc.midiConnections.model.add(Ref(nil));

		wmc.midiDisplay ?? { wmc.midiDisplay = () };
		wmc.midiDisplay.model ?? {
			wmc.midiDisplay.model = List[];
		};
		wmc.midiDisplay.model.add(Ref((
			src: "source",
			chan: "chan",
			ctrl: "ctrl",
			learn: "L"
		)));
		this.initControllers(wmc);
	}

	initControllers { |wmc|
		#[
			prInitMidiOptions,
			prInitMidiConnection,
			prInitMidiDisplay
		].do { |method|
			this.perform(method, wmc, widget.cv)
		}
	}

	// private: default controllers
	prInitMidiOptions { |mc, cv|
		mc.midiOptions.controller ?? {
			mc.midiOptions.controller = SimpleController(mc.midiOptions.model);
		};
		mc.midiOptions.controller.put(\default, { |changer, what ... moreArgs|
			// ...
		})
	}

	prInitMidiConnection { |mc, cv|
		var ccAction, makeCCconnection;
		var slotChanger;
		mc.midiConnections.controller ?? {
			mc.midiConnections.controller = SimpleController(mc.midiConnections.model);
		};
		mc.midiConnections.controller.put(\default, { |changer, what ... moreArgs|
			var index = moreArgs[0];

			if (changer[index].value.class == Event) {
				slotChanger = changer[index].value;
				// connect
				ccAction = { |val, num, chan, src|
					// if we only data structure to hold connections is the model
					// we must infer the connections parameters here
					if (mc.midiConnections.model[index].value.isEmpty) {
						mc.midiConnections.model[index].value_((num: num, chan: chan, src: src))
					};
					this.getMidiMode.switch(
						//  0-127
						0, {
							if ((this.getSoftWithin <= 0).or(
								val/127 < (cv.input + (this.getSoftWithin/2)) and: {
									val/127 > (cv.input - (this.getSoftWithin/2))
							})) {
								cv.input_(val/127);
								// [val, cv.input, cv.value].postln;
							};
						},
						// +/-
						1, {
							cv.input_(cv.input + (val-this.getMidiMean/127*this.getMidiResolution));
							// [val, cv.input, cv.value].postln;
						}
					)
				};
				makeCCconnection = { |argSrc, argChan, argNum|
					// no need to create a new MIDIFunc any time midiDisconnect is called
					// midiFunc remains anyway - re-use instead of overwriting and potentially
					// creating a pile of orphaned MIDIFuncs (memory leak)
					if (midiFunc.isNil or: { midiFunc.func.isNil }) {
						midiFunc = MIDIFunc.cc(ccAction, argNum, argChan, argSrc);
					} {
						midiFunc.add(ccAction);
					}
				};

				if (slotChanger.isEmpty) {
					"MIDIFunc should learn".inform;
					makeCCconnection.().learn;
				} {
					"MIDIFunc was set to src: %, channel: %, number: %".format(slotChanger.src, slotChanger.chan, slotChanger.num).inform;
					makeCCconnection.(slotChanger.src, slotChanger.chan, slotChanger.num);
				};
			} {
				midiFunc.clear;
			};
		})
	}

	prInitMidiDisplay { |mc, cv|
		mc.midiDisplay.controller ?? {
			mc.midiDisplay.controller = SimpleController(mc.midiDisplay.model);
		};
		mc.midiDisplay.controller.put(\default, { |changer, what ... moreArgs|
			// ...
		})
	}

	setMidiMode { |mode|
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};

		mc.midiOptions.model[index].value_((
			midiMode: mode,
			midiMean: mc.midiOptions.model[index].value.midiMean,
			ctrlButtonBank: mc.midiOptions.model[index].value.ctrlButtonBank,
			midiResolution: mc.midiOptions.model[index].value.midiResolution,
			softWithin: mc.midiOptions.model[index].value.softWithin
		)).changedKeys(widget.syncKeys, index);
	}

	getMidiMode {
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		^mc.midiOptions.model[index].value.midiMode;
	}

	setMidiMean { |meanval|
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		meanval = meanval.asInteger;

		mc.midiOptions.model[index].value_((
			midiMode: mc.midiOptions.model[index].value.midiMode,
			midiMean: meanval,
			ctrlButtonBank: mc.midiOptions.model[index].value.ctrlButtonBank,
			midiResolution: mc.midiOptions.model[index].value.midiResolution,
			softWithin: mc.midiOptions.model[index].value.softWithin
		)).changedKeys(widget.syncKeys, index);
	}

	getMidiMean {
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		^mc.midiOptions.model[index].value.midiMean;
	}

	setSoftWithin { |threshold|
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		threshold = threshold.asFloat;

		mc.midiOptions.model[index].value_((
			midiMode: mc.midiOptions.model[index].value.midiMode,
			midiMean: mc.midiOptions.model[index].value.midiMean,
			ctrlButtonBank: mc.midiOptions.model[index].value.ctrlButtonBank,
			midiResolution: mc.midiOptions.model[index].value.midiResolution,
			softWithin: threshold
		)).changedKeys(widget.syncKeys, index);
	}

	getSoftWithin {
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		^mc.midiOptions.model[index].value.softWithin;
	}

	setCtrlButtonBank { |numSliders|
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		if (numSliders.notNil and:{ numSliders.isInteger.not }) {
			Error("setCtrlButtonBank: 'numSliders' must either be an Integer or nil!").throw;
		};

		mc.midiOptions.model[index].value_((
			midiMode: mc.midiOptions.model[index].value.midiMode,
			midiMean: mc.midiOptions.model[index].value.midiMean,
			ctrlButtonBank: numSliders,
			midiResolution: mc.midiOptions.model[index].value.midiResolution,
			softWithin: mc.midiOptions.model[index].value.softWithin
		)).changedKeys(widget.syncKeys, index);
	}

	getCtrlButtonBank {
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		^mc.midiOptions.model[index].value.ctrlButtonBank;
	}

	setMidiResolution { |resolution|
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		mc.midiOptions.model[index].value_((
			midiMode: mc.midiOptions.model[index].value.midiMode,
			midiMean: mc.midiOptions.model[index].value.midiMean,
			ctrlButtonBank: mc.midiOptions.model[index].value.ctrlButtonBank,
			midiResolution: resolution,
			softWithin: mc.midiOptions.model[index].value.softWithin
		)).changedKeys(widget.syncKeys, index);
	}

	getMidiResolution {
		var index = widget.midiConnections.indexOf(this);
		^widget.wmc.midiOptions.model[index].value.midiResolution;
	}

	midiConnect { |src, chan, num|
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		mc.midiConnections.model[index].value_(
			(src: src, chan: chan, num: num)
		);
		mc.midiConnections.model.changedKeys(widget.syncKeys, index);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? {
				this.midiFunc.permanent_(this.widget.class.removeResponders)
			}
		})
	}

	midiDisconnect {
		var mc = widget.wmc;
		var index = widget.midiConnections.indexOf(this);
		mc.midiConnections.model[index].value_(nil);
		mc.midiConnections.model.changedKeys(widget.syncKeys, index);
	}

	gui { |parent, bounds|

	}

	remove {
		var index = widget.midiConnections.indexOf(this);
		this.midiDisconnect;
		midiFunc.free;
		widget.wmc.midiConnections.model.removeAt(index);
		widget.midiConnections.remove(this).changed(\value);
	}
}

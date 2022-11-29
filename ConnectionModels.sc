OscConnection {
	classvar cAnons = 0;
	var <widget, <>name;
	var <mc; // models and controllers

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
		this.initModels;
	}

	initModels { |modelsControllers|
		if (modelsControllers.notNil) {
			mc = modelsControllers;
		} {
			mc ?? { mc = () };
		};

		mc.oscCalibration ?? { mc.oscCalibration = () };
		mc.oscCalibration.model ?? {
			mc.oscCalibration.model = Ref(CVWidget.oscCalibration);
		};

		mc.oscInputRange ?? { mc.oscInputRange = () };
		mc.oscInputRange.model ?? {
			mc.oscInputRange.model = Ref([0.0001, 0.0001])
		};

		mc.oscConnection ?? { mc.oscConnection = () };
		mc.oscConnection.model ?? {
			mc.oscConnection.model = Ref(false);
		};

		mc.oscDisplay ?? { mc.oscDisplay = () };
		mc.oscDisplay.model ?? {
			mc.oscDisplay.model = Ref((
				ipField: nil,
				portField: nil,
				nameField: "/my/cmd/name",
				index: 1,
				connectorButVal: 0,
				editEnabled: true
			))
		};

		// add model to list of connection
		// models in the instance' model
		widget.wmc.osc.add(mc);

		this.initControllers;
	}

	initControllers {
		#[
			prInitOscCalibration,
			prInitOscInputRange,
			prInitOscConnect,
			prInitOscDisplay
		].do { |method|
			this.perform(method, mc, widget.cv)
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
		mc.oscConnection.controller ?? {
			mc.oscConnection.controller = SimpleController(mc.oscConnection.model)
		};
		mc.oscConnection.controller.put(\default, { |changer, what, moreArgs|
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
		^nil;
	}

	oscConnect {}
	oscDisconnect {}
}

MidiConnection {
	classvar cAnons = 0;
	var <widget, <>name;
	var <mc; // models and controllers
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
		this.initModels;
	}

	initModels { |modelsControllers|
		if (modelsControllers.notNil) {
			mc = modelsControllers;
		} {
			mc ?? { mc = () };
		};

		mc.midiOptions ?? { mc.midiOptions = () };
		mc.midiOptions.model ?? {
			mc.midiOptions.model = Ref((
				midiMode: CVWidget.midiMode,
				midiMean: CVWidget.midiMean,
				ctrlButtonBank: CVWidget.ctrlButtonBank,
				midiResolution: CVWidget.midiResolution,
				softWithin: CVWidget.softWithin
			))
		};

		mc.midiConnection ?? { mc.midiConnection = () };
		mc.midiConnection.model ?? {
			mc.midiConnection.model = Ref(nil);
		};

		mc.midiDisplay ?? { mc.midiDisplay = () };
		mc.midiDisplay.model ?? {
			mc.midiDisplay.model = Ref((
				src: "source",
				chan: "chan",
				ctrl: "ctrl",
				learn: "L"
			))
		};

		// add model to list of connection
		// models in the instance' model
		widget.wmc.midi.add(mc);

		this.initControllers
	}

	initControllers {
		#[
			prInitMidiOptions,
			prInitMidiConnect,
			prInitMidiDisplay
		].do { |method|
			this.perform(method, mc, widget.cv)
		}
	}

	// private: default controllers
	prInitMidiOptions { |mc, cv|
		mc.midiOptions.controller ?? {
			mc.midiOptions.controller = SimpleController(mc.midiOptions.model);
		};
		mc.midiOptions.controller.put(\default, { |changer, what, moreArgs|
			// ...
		})
	}

	prInitMidiConnect { |mc, cv|
		var ccAction, makeCCconnection;

		mc.midiConnection.controller ?? {
			mc.midiConnection.controller = SimpleController(mc.midiConnection.model);
		};
		mc.midiConnection.controller.put(\default, { |changer, what, moreArgs|
			if (changer.value.class == Event) {
				// connect
				ccAction = { |val, num, chan, src|
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
					if (midiFunc.isNil) {
						midiFunc = MIDIFunc.cc(ccAction, argNum, argChan, argSrc);
					} {
						midiFunc.add(ccAction)
					}
				};

				if (changer.value.isEmpty) {
					"MIDIFunc should learn".inform;
					makeCCconnection.().learn;
				} {
					"MIDIFunc was set to src: %, channel: %, number: %".format(changer.value.src, changer.value.chan, changer.value.num).inform;
					makeCCconnection.(changer.value.src, changer.value.chan, changer.value.num);
				}
			} {
				midiFunc.clear;
			};
		})
	}

	prInitMidiDisplay {
		mc.midiDisplay.controller ?? {
			mc.midiDisplay.controller = SimpleController(mc.midiDisplay.model);
		};
		mc.midiDisplay.controller.put(\default, { |changer, what, moreArgs|
			// ...
		})
	}

	setMidiMode { |mode|
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};

		mc.midiOptions.model.value_((
			midiMode: mode,
			midiMean: mc.midiOptions.model.value.midiMean,
			ctrlButtonBank: mc.midiOptions.model.value.ctrlButtonBank,
			midiResolution: mc.midiOptions.model.value.midiResolution,
			softWithin: mc.midiOptions.model.value.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiMode {
		^mc.midiOptions.model.value.midiMode;
	}

	setMidiMean { |meanval|
		meanval = meanval.asInteger;

		mc.midiOptions.model.value_((
			midiMode: mc.midiOptions.model.value.midiMode,
			midiMean: meanval,
			ctrlButtonBank: mc.midiOptions.model.value.ctrlButtonBank,
			midiResolution: mc.midiOptions.model.value.midiResolution,
			softWithin: mc.midiOptions.model.value.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiMean {
		^mc.midiOptions.model.value.midiMean;
	}

	setSoftWithin { |threshold|
		threshold = threshold.asFloat;

		mc.midiOptions.model.value_((
			midiMode: mc.midiOptions.model.value.midiMode,
			midiMean: mc.midiOptions.model.value.midiMean,
			ctrlButtonBank: mc.midiOptions.model.value.ctrlButtonBank,
			midiResolution: mc.midiOptions.model.value.midiResolution,
			softWithin: threshold
		)).changedKeys(widget.syncKeys);
	}

	getSoftWithin {
		^mc.midiOptions.model.value.softWithin;
	}

	setCtrlButtonBank { |numSliders|
		if (numSliders.notNil and:{ numSliders.isInteger.not }) {
			Error("setCtrlButtonBank: 'numSliders' must either be an Integer or nil!").throw;
		};

		mc.midiOptions.model.value_((
			midiMode: mc.midiOptions.model.value.midiMode,
			midiMean: mc.midiOptions.model.value.midiMean,
			ctrlButtonBank: numSliders,
			midiResolution: mc.midiOptions.model.value.midiResolution,
			softWithin: mc.midiOptions.model.value.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getCtrlButtonBank {
		^mc.midiOptions.model.value.ctrlButtonBank;
	}

	setMidiResolution { |resolution|
		mc.midiOptions.model.value_((
			midiMode: mc.midiOptions.model.value.midiMode,
			midiMean: mc.midiOptions.model.value.midiMean,
			ctrlButtonBank: mc.midiOptions.model.value.ctrlButtonBank,
			midiResolution: resolution,
			softWithin: mc.midiOptions.model.value.softWithin
		)).changedKeys(widget.syncKeys);
	}

	getMidiResolution {
		^mc.midiOptions.model.value.midiResolution;
	}

	midiConnect { |src, chan, num|
		// any further checks

		mc.midiConnection.model.value_(
			(src: src, chan: chan, num: num)
		).changedKeys(widget.syncKeys);
		// TODO - check settings system
		CmdPeriod.add({
			this.widget !? {
				this.midiFunc.permanent_(this.widget.class.removeResponders)
			}
		})
	}

	midiDisconnect {
		mc.midiConnection.model.value_(nil).changedKeys(widget.syncKeys);
	}

	gui { |parent, bounds|

	}

	remove {
		this.midiDisconnect;
		widget.midiConnections.remove(this).changed(\value);
		midiFunc.remove;
	}
}

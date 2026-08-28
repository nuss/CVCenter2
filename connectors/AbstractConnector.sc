AbstractConnector {
	var <widget;

	index { this.subclassResponsibility(thisMethod) }
	name { this.subclassResponsibility(thisMethod) }
	name_ { this.subclassResponsibility(thisMethod) }
	remove { this.subclassResponsibility(thisMethod) }

	// MidiConnectors
	// options
	setMidiOption { this.subclassResponsibility(thisMethod) }
	getMidiOption  { this.subclassResponsibility(thisMethod) }

	setMidiMode { |mode|
		// 14-bit MIDI mode?
		if (mode.asInteger != 0 and:{ mode.asInteger != 1 }) {
			Error("setMidiMode: 'mode' must either be 0 or 1!").throw;
		};
		this.setMidiOption(\midiMode, mode)
	}

	getMidiMode {
		^this.getMidiOption(\midiMode);
	}

	setMidiZero { |zeroval|
		this.setMidiOption(\midiZero, zeroval.asInteger)
	}

	getMidiZero {
		^this.getMidiOption(\midiZero);
	}

	setMidiSnapDistance { |snapDistance|
		this.setMidiOption(\snapDistance, snapDistance.asFloat)
	}

	getMidiSnapDistance {
		^this.getMidiOption(\snapDistance)
	}

	setMidiCtrlButtonGroup { |numButtons|
		if (numButtons.notNil and:{ numButtons.isInteger.not }) {
			Error("setMidiCtrlButtonGroup: 'numButtons' must either be an Integer or nil!").throw;
		};
		this.setMidiOption(\ctrlButtonGroup, numButtons)
	}

	getMidiCtrlButtonGroup {
		^this.getMidiOption(\ctrlButtonGroup)
	}

	setMidiResolution { |resolution|
		this.setMidiOption(\midiResolution, resolution)
	}

	getMidiResolution {
		^this.getMidiOption(\midiResolution);
	}

	setMidiInputMapping { this.subclassResponsibility(thisMethod) }

	getMidiInputMapping {
		^this.getMidiOption(\midiInputMapping)
	}

	// display values
	setMidiDisplay { this.subclassResponsibility(thisMethod) }
	getMidiDisplay { this.subclassResponsibility(thisMethod) }

	setMidiTemplate { |argTemplate|
		this.setMidiDisplay(\template, argTemplate.cs)
	}

	getMidiTemplate {
		^this.getMidiDisplay(\template).interpret
	}

	setMidiDispatcher { |dispatcher|
		this.setMidiDisplay(\dispatcher, dispatcher.cs)
	}

	getMidiDispatcher {
		^this.getMidiDisplay(\dispatcher).interpret;
	}

	// connections
	setMIDIFuncEnabled { this.subclassResponsibility(thisMethod) }
	getMIDIFuncEnabled { this.subclassResponsibility(thisMethod) }
	midiConnect { this.subclassResponsibility(thisMethod) }
	midiDisconnect { this.subclassResponsibility(thisMethod) }

	// OscConnectors
	// options
	setOscOption { this.subclassResponsibility(thisMethod) }
	getOscOption { this.subclassResponsibility(thisMethod) }

	setOscEndless { |boolEndless|
		this.setOscOption(\oscEndless, boolEndless)
	}

	getOscEndless {
		^this.getOscOption(\oscEndless)
	}

	setOscResolution { |resolution|
		this.setOscOption(\oscResolution, resolution)
	}

	getOscResolution {
		^this.getOscOption(\oscResolution)
	}

	setOscSnapDistance { |distance|
		this.setOscOption(\oscSnapDistance, distance)
	}

	getOscSnapDistance {
		^this.getOscOption(\oscSnapDistance)
	}

	setOscCalibration { |boolCalibration|
		this.setOscOption(\oscCalibration, boolCalibration)
	}

	getOscCalibration {
		^this.getOscOption(\oscCalibration)
	}

	resetOscCalibration {
		this.setOscOption(\oscInputRange, CVWidget.oscInputRange);
	}

	setOscInputConstraints { this.subclassResponsibility(thisMethod) }

	getOscInputConstraints {
		^this.getOscOption(\oscInputRange)
	}

	setOscInputMapping { this.subclassResponsibility(thisMethod) }

	getOscInputMapping {
		^this.getOscOption(\oscInputMapping)
	}

	setOscMatching { |boolMatching|
		this.setOscOption(\oscMatching, boolMatching)
	}

	getOscMatching {
		^this.getOscOption(\oscMatching)
	}

	// display values
	setOscDisplay { this.subclassResponsibility(thisMethod) }
	getOscDisplay { this.subclassResponsibility(thisMethod) }

	setOscCmdName { |cmdPath|
		this.setOscDisplay(\nameField, cmdPath.asSymbol)
	}

	getOscCmdName {
		^this.getOscDisplay(\nameField)
	}

	setOscInputAlwaysPositive { |value|
		this.setOscDisplay(\alwaysPositive, value)
	}

	getOscInputAlwaysPositive {
		^this.getOscDisplay(\alwaysPositive)
	}

	setOscMsgIndex { |msgIndex|
		this.setOscDisplay(\msgSlot, msgIndex.asInteger)
	}

	getOscMsgIndex {
		^this.getOscDisplay(\msgSlot);
	}

	setOscTemplate { |argTemplate|
		this.setOscDisplay(\oscTemplate, argTemplate.cs)
	}

	getOscTemplate {
		^this.getOscDisplay(\oscTemplate)
	}

	setOscDispatcher { |dispatcher|
		this.setOscDisplay(\dispatcher, dispatcher)
	}

	getOscDispatcher {
		^this.getOscDisplay(\dispatcher)
	}

	// connections
	setOSCFuncEnabled { this.subclassResponsibility(thisMethod) }
	getOSCFuncEnabled { this.subclassResponsibility(thisMethod) }
	oscConnect { this.subclassResponsibility(thisMethod) }
	oscDisconnect { this.subclassResponsibility(thisMethod) }
}
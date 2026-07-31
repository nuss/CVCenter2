AbstractConnector {
	var <widget;

	// MidiConnectors

	// OscConnectors
	index { this.subclassResponsibility(thisMethod) }
	name { this.subclassResponsibility(thisMethod) }
	name_ { this.subclassResponsibility(thisMethod) }
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
		this.setOscDisplay(\index, msgIndex.asInteger)
	}

	getOscMsgIndex {
		^this.getOscDisplay(\index);
	}

	setOscMatching { |boolMatching|
		this.oscDisplay(\oscMatching, boolMatching)
	}

	getOscMatching {
		^this.getOscDisplay(\oscMatching)
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

}
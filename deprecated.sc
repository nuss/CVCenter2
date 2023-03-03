+CVWidgetKnob {
	setMidiMean { |zeroval, connector|
		this.deprecated(thisMethod, CVWidgetKnob.findMethod(\setMidiZero));
		^this.setMidiZero(zeroval, connector);
	}

	getMidiMean { |connector|
		this.deprecated(thisMethod, CVWidgetKnob.findMethod(\getMidiZero));
		^this.getMidiZero(connector);
	}

	setSoftWithin { |snapDistance, connector|
		this.deprecated(thisMethod, CVWidgetKnob.findMethod(\setSnapDistance));
		^this.setSnapDistance(snapDistance, connector);
	}

	getSoftWithin { |connector|
		this.deprecated(thisMethod, CVWidgetKnob.findMethod(\getSnapDistance));
		^this.getSnapDistance(connector);
	}

	setCtrlButtonBank { |numButtons, connector|
		this.deprecated(thisMethod, CVWidgetKnob.findMethod(\setCtrlButtonGroup));
		^this.setCtrlButtonGroup(numButtons, connector);
	}

	getCtrlButtonBank { |connector|
		this.deprecated(thisMethod, CVWidgetKnob.findMethod(\getCtrlButtonGroup));
		^this.getCtrlButtonGroup(connector);
	}
}

+MidiConnector {
	setMidiMean { |zeroval|
		this.deprecated(thisMethod, MidiConnector.findMethod(\setMidiZero));
		^this.setMidiZero(zeroval);
	}

	getMidiMean {
		this.deprecated(thisMethod, MidiConnector.findMethod(\getMidiZero));
		^this.getMidiZero;
	}

	setSoftWithin { |snapDistance|
		this.deprecated(thisMethod, MidiConnector.findMethod(\setSnapDistance));
		^this.setSnapDistance(snapDistance);
	}

	getSoftWithin {
		this.deprecated(thisMethod, MidiConnector.findMethod(\getSnapDistance));
		^this.getSnapDistance;
	}

	setCtrlButtonBank { |numButtons|
		this.deprecated(thisMethod, MidiConnector.findMethod(\setCtrlButtonGroup));
		^this.setCtrlButtonGroup(numButtons);
	}

	getCtrlButtonBank {
		this.deprecated(thisMethod, MidiConnector.findMethod(\getCtrlButtonGroup));
		^this.getCtrlButtonGroup;
	}

}
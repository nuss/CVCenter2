+CVWidgetKnob {
	setMidiMean { |zeroval, connector|
		this.deprecated(thisMethod, this.class.findMethod(\setMidiZero));
		^this.setMidiZero(zeroval, connector);
	}

	getMidiMean { |connector|
		this.deprecated(thisMethod, this.class.findMethod(\getMidiZero));
		^this.getMidiZero(connector);
	}

	setSoftWithin { |snapDistance, connector|
		this.deprecated(thisMethod, this.class.findMethod(\setMidiSnapDistance));
		^this.setMidiSnapDistance(snapDistance, connector);
	}

	getSoftWithin { |connector|
		this.deprecated(thisMethod, this.class.findMethod(\getMidiSnapDistance));
		^this.getMidiSnapDistance(connector);
	}

	setCtrlButtonBank { |numButtons, connector|
		this.deprecated(thisMethod, this.class.findMethod(\setMidiCtrlButtonGroup));
		^this.setMidiCtrlButtonGroup(numButtons, connector);
	}

	getCtrlButtonBank { |connector|
		this.deprecated(thisMethod, this.class.findMethod(\getMidiCtrlButtonGroup));
		^this.getMidiCtrlButtonGroup(connector);
	}
}

+MidiConnector {
	setMidiMean { |zeroval|
		this.deprecated(thisMethod, this.class.findMethod(\setMidiZero));
		^this.setMidiZero(zeroval);
	}

	getMidiMean {
		this.deprecated(thisMethod, this.class.findMethod(\getMidiZero));
		^this.getMidiZero;
	}

	setSoftWithin { |snapDistance|
		this.deprecated(thisMethod, this.class.findMethod(\setMidiSnapDistance));
		^this.setMidiSnapDistance(snapDistance);
	}

	getSoftWithin {
		this.deprecated(thisMethod, this.class.findMethod(\getMidiSnapDistance));
		^this.getMidiSnapDistance;
	}

	setCtrlButtonBank { |numButtons|
		this.deprecated(thisMethod, this.class.findMethod(\setMidiCtrlButtonGroup));
		^this.setMidiCtrlButtonGroup(numButtons);
	}

	getCtrlButtonBank {
		this.deprecated(thisMethod, this.class.findMethod(\getMidiCtrlButtonGroup));
		^this.getMidiCtrlButtonGroup;
	}

}

+Object {

	changedKeys { |keys ... moreArgs|
		this.deprecated(thisMethod, this.class.findMethod(\changedPerformKeys));
		^this.changedPerformKeys(keys, *moreArgs);
	}

}
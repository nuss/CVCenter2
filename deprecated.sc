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
		this.deprecated(thisMethod, this.class.findMethod(\setSnapDistance));
		^this.setSnapDistance(snapDistance, connector);
	}

	getSoftWithin { |connector|
		this.deprecated(thisMethod, this.class.findMethod(\getSnapDistance));
		^this.getSnapDistance(connector);
	}

	setCtrlButtonBank { |numButtons, connector|
		this.deprecated(thisMethod, this.class.findMethod(\setCtrlButtonGroup));
		^this.setCtrlButtonGroup(numButtons, connector);
	}

	getCtrlButtonBank { |connector|
		this.deprecated(thisMethod, this.class.findMethod(\getCtrlButtonGroup));
		^this.getCtrlButtonGroup(connector);
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
		this.deprecated(thisMethod, this.class.findMethod(\setSnapDistance));
		^this.setSnapDistance(snapDistance);
	}

	getSoftWithin {
		this.deprecated(thisMethod, this.class.findMethod(\getSnapDistance));
		^this.getSnapDistance;
	}

	setCtrlButtonBank { |numButtons|
		this.deprecated(thisMethod, this.class.findMethod(\setCtrlButtonGroup));
		^this.setCtrlButtonGroup(numButtons);
	}

	getCtrlButtonBank {
		this.deprecated(thisMethod, this.class.findMethod(\getCtrlButtonGroup));
		^this.getCtrlButtonGroup;
	}

}

+Object {

	changedKeys { |keys ... moreArgs|
		this.deprecated(thisMethod, this.class.findMethod(\changedPerformKeys));
		^this.changedPerformKeys(keys, *moreArgs);
	}

}
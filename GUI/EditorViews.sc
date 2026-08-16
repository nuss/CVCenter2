OscConnectorsEditorView : SCViewHolder {
	classvar <all;
	var <widget, <slot, <parent, <cIndex;
	var <connector, connectors;
	// GUI elements
	var <e;

	*initClass {
		all = ();
	}

	*new { |parent, rect(Rect(0, 0, 300, 600)), widget, slot, connector(0)|
		^super.newCopyArgs(widget: widget, slot: slot).init(connector, parent, rect);
	}

	init { |index, parentView, rect|
		var o;

		connectors = switch (widget.class)
		{ CVWidgetKnob } {
			widget.wmc.oscConnectors.m.value
		}
		{ CVWidgetMS } {
			widget.wmc.oscConnectors.m[this.slot].value
		};

		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = connectors.detect { |c| c.name == index }
		};
		if (index.class == OscConnector) {
			index = connectors.indexOf(index)
		};
		// fallback if index out of bounds
		if (index.isNil or: { index > connectors.lastIndex }) { index = 0 };

		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		if (parentView.isNil) {
			parent = switch (widget.class)
			{ CVWidgetKnob } {
				Window("%: OSC connections".format(widget.name), rect)
			}
			{ CVWidgetMS } {
				Window("%[%]: OSC connections".format(widget.name, this.slot), rect)
			};
			this.view = parent.view;
		} {
			parent = parentView;
			this.view = View(parent, rect);
		};

		parent.onClose_({ this.close });

		if (connectors.isEmpty) {
			switch (widget.class)
			{ CVWidgetKnob } {
				OscConnector(widget)
			}
			{ CVWidgetMS } {
				OscConnectorMS(widget, slot: this.slot)
			}
		};

		e = ();
		e.connectorNameField = ConnectorNameField(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \osc);
		e.connectorSelect = ConnectorSelect(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \osc);
		e.addrAndCmdSelect = OscSelectsComboView(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.oscCmdTextField = OscCmdNameField(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.oscMsgIndexNumBox = OscMsgIndexBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index).value_(widget.wmc.oscDisplay.m.value[index].index);
		e.oscPatternMatchingCheckBox = OscMatchingCheckBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.oscModeSelect = OscModeSelect(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.oscResolutionNumBox = OscResolutionBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.oscSnapDistanceNumBox = OscSnapDistanceNumBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.inputConstraintsLoNumBox = OscConstrainterNumBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, position: 0);
		e.inputConstraintsHiNumBox = OscConstrainterNumBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, position: 1);
		e.zeroCrossCorrectStaticText = OscZeroCrossingText(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.calibrationButton = OscCalibrationButton(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.resetButton = OscCalibrationResetButton(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.specStaticText = ControlSpecText(parent, widget);
		e.inOutMappingSelect = MappingSelect(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \osc);
		e.connectorButton = OscConnectButton(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.oscPlayPauseButton = PlayPauseButton(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \osc);
		e.removeConnectorButton = ConnectorRemoveButton(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \osc);

		this.view.layout_(
			VLayout(
				HLayout(
					[e.connectorNameField, stretch: 9],
					[e.connectorSelect, stretch: 1]
				),
				HLayout(
					[e.addrAndCmdSelect]
				),
				HLayout(
					StaticText(parent).string_("OSC command name - either select from list provided by the selected device or set custom one")
				),
				HLayout(
					VLayout(
						[e.deviceSelect],
						[e.oscCmdSelect]
					),
					[e.newDeviceBut]
				),
				HLayout(
					[e.oscCmdTextField],
					[e.oscMsgIndexNumBox],
					[e.oscPatternMatchingCheckBox]
				),
				HLayout(
					[StaticText(parent).string_("OSC mode: absolute value or in-/decremental (endless):"), stretch: 7],
					[e.oscModeSelect, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("OSC resolution ('endless' mode only)"), stretch: 7],
					[e.oscResolutionNumBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("snap distance for slider ('absolute' mode only)"), stretch: 7],
					[e.oscSnapDistanceNumBox, stretch: 3]
				),
				HLayout(
					StaticText(parent).string_("OSC input constraints, zero-crossing correction")
				),
				HLayout(
					[e.inputConstraintsLoNumBox],
					[e.inputConstraintsHiNumBox],
					[e.zeroCrossCorrectStaticText],
					[e.calibrationButton],
					[e.resetButton]
				),
				HLayout(
					[e.inOutMappingSelect]
				),
				HLayout(
					[e.specStaticText]
				),
				HLayout(
					[e.connectorButton, stretch: 5],
					[e.oscPlayPauseButton, stretch: 1],
					[e.removeConnectorButton, stretch: 5]
				)
			)
		);

		e.connectorSelect.view.action_({ |sel|
			if (sel.value == (sel.items.size - 1)) {
				o = widget.addOscConnector(slot: this.slot);
				e.connectorSelect.view.value_(connectors.indexOf(o));
			};

			if (sel.value < (sel.items.size - 1)) {
				e.do(_.index_(sel.value));
			}
		})
	}

	// connector can be a numeric index or a connector object
	set { |connector|
		if (connector.isInteger) {
			connector = connectors[connector]
		};
		cIndex = connectors.indexOf(connector);
		e.do(_.index_(cIndex));
	}

	front {
		parent.front;
	}

	setWidget { |otherWidget, slot|
		if (widget.class === CVWidgetMS and: { slot.isNil }) {
			Error("Setting an OscConnectorsEditorView for a CVWidgetMS requires a numeric slot.").throw;\
		};
		all[widget].remove(this);
		widget = otherWidget;
		connectors = switch (widget.class)
		{ CVWidgetKnob } {
			widget.wmc.oscConnectors.m.value
		}
		{ CVWidgetMS } {
			widget.wmc.oscConnectors.m[this.slot].value
		};
		connector = connectors[0];
		all[widget] ?? { all[widget] = List[] };
		if (all[widget].includes(this).not) { all[widget].add(this) };
		e.do(_.setWidget(widget, slot));
		if (parent.class === Window) {
			switch (widget.class)
			{ CVWidgetKnob } {
				parent.name_("%: OSC connections".format(widget.name))
			}
			{ CVWidgetMS } {
				parent.name_("%[%]: OSC connections".format(widget.name, slot))
			}
		}
	}

	close {
		all[widget].remove(this);
		e.do(_.close);
	}

	*closeAll {
		all.pairsDo { |key, eds|
			// IMPORTANT
			// with each call to 'close' the index into the list of editors
			// advances by 1. However, as the first call will already have
			// removed the editor at index 0 the next call will not remove
			// the editor at index 1 but the editor at index 2 which has meanwhile
			// become index 1. Hence, every second editor will be omitted if
			// the list of editors isn't reversed before invoking the loop by
			// calling 'do'!
			eds.reverse.do(_.close)
		}
	}
}

MidiConnectorsEditorView : SCViewHolder {
	classvar <all;
	var <widget, <slot, <parent, <index;
	var <connector, connectors, connections;
	// GUI elements
	var <e;

	*initClass {
		all = ();
	}

	*new { |parent, rect(Rect(0, 0, 300, 440)), widget, slot, connector(0)|
		^super.newCopyArgs(widget: widget, slot: slot).init(connector, parent, rect);
	}

	init { |index, parentView, rect|
		var m;

		switch (widget.class)
		{ CVWidgetKnob } {
			connectors = widget.wmc.midiConnectors.m.value;
			connections = widget.wmc.midiConnections.m.value;
		}
		{ CVWidgetMS } {
			connectors = widget.wmc.midiConnectors.m[this.slot].value;
			connections = widget.wmc.midiConnections.m[this.slot].value;
		};

		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = connectors.detect { |c| c.name == index }
		};
		if (index.class == MidiConnector) {
			index = connectors.indexOf(index)
		};
		// after all, if index is nil or greater than the last index of widget.midiConnectors set it to 0
		if (index.isNil or: { index > connectors.lastIndex }) { index = 0 };

		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		if (parentView.isNil) {
			parent = switch (widget.class)
			{ CVWidgetKnob } {
				Window("%: MIDI connections".format(widget.name), rect)
			}
			{ CVWidgetMS } {
				Window("%[%]: MIDI connections".format(widget.name, this.slot), rect)
			};
			this.view = parent.view;
		} {
			parent = parentView;
			this.view = View(parent, rect);
		};

		parent.onClose_({ this.close });

		if (connectors.isEmpty) {
			switch (widget.class)
			{ CVWidgetKnob } {
				MidiConnector(widget)
			}
			{ CVWidgetMS } {
				MidiConnector(widget, slot: this.slot)
			}
		};

		e = ();
		e.connectorNameField = ConnectorNameField(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \midi);
		e.connectorSelect = ConnectorSelect(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \midi);
		e.midiModeSelect = MidiModeSelect(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.midiZeroBox = MidiZeroNumberBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.snapDistanceBox = SnapDistanceNumberBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.midiResolutionBox = MidiResolutionNumberBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.slidersPerGroupNB = SlidersPerGroupNumberBox(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.midiLearnButton = MidiLearnButton(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.midiPlayPauseButton = PlayPauseButton(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \midi);
		e.midiSrcSelect = MidiSrcSelect(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.midiChanTF = MidiChanField(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.midiNumTF = MidiCtrlField(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index);
		e.mappingSelect = MappingSelect(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \midi);
		e.specStaticText = ControlSpecText(parent, widget);
		e.midiInit = MidiInitButton(parent);
		e.midiConnectorRemove = ConnectorRemoveButton(parent, widget, slot: slot !? { slot.asInteger }, connectorID: index, connectorKind: \midi);

		this.view.layout_(
			VLayout(
				HLayout(
					[e.connectorNameField, stretch: 9],
					[e.connectorSelect, stretch: 1]
				),
				HLayout(
					[StaticText(parent).string_("MIDI mode: 0-127 or endless:"), stretch: 7],
					[e.midiModeSelect, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("MIDI neutral ('endless' mode only): "), stretch: 7],
					[e.midiZeroBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("snap distance for slider (0-127 only): "), stretch: 7],
					[e.snapDistanceBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("MIDI resolution (endless mode only): "), stretch: 7],
					[e.midiResolutionBox, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("Number of sliders per bank: "), stretch: 7],
					[e.slidersPerGroupNB, stretch: 3]
				),
				HLayout(
					[e.mappingSelect]
				),
				HLayout(
					[e.midiLearnButton, stretch: 1],
					[e.midiPlayPauseButton, stretch: 1],
					[e.midiSrcSelect, stretch: 6],
					[e.midiChanTF, stretch: 2],
					[e.midiNumTF, stretch: 2]
				),
				HLayout(
					[e.specStaticText]
				),
				HLayout(
					[e.midiInit],
					[e.midiConnectorRemove]
				)
			)
		);

		e.connectorSelect.view.action_({ |sel|
			if (sel.value == (sel.items.size - 1)) {
				m = widget.addMidiConnector(slot: this.slot);
				e.connectorSelect.view.value_(widget.midiConnectors.indexOf(m));
			};

			if (sel.value < (sel.items.size - 1)) {
				e.do(_.index_(sel.value));
				// enable or disable selects for MIDI source, channel and ctrl number based on connection status
				[e.midiSrcSelect, e.midiChanTF, e.midiNumTF].do { |elem|
					elem.view.enabled_(connections[sel.value].isNil)
				}
			}
		})
	}

	set { |connector|
		if (connector.isInteger) {
			index = connector
		} {
			index = connectors.indexOf(connector)
		};
		e.do(_.index_(index));
	}

	setWidget { |otherWidget, slot|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[widget].remove(this);
		widget = otherWidget;
		switch (widget.class)
		{ CVWidgetKnob } {
			connectors = widget.wmc.midiConnectors.m.value;
			connections = widget.wmc.midiConnections.m.value;
		}
		{ CVWidgetMS } {
			connectors = widget.wmc.midiConnectors.m[this.slot].value;
			connections = widget.wmc.midiConnections.m[this.slot].value;
		};
		connector = connections[0];
		index = 0;
		all[widget] ?? { all[widget] = List[] };
		if (all[widget].includes(this).not) { all[widget].add(this) };
		e.do(_.setWidget(widget));
		if (parent.class === Window) {
			switch (widget.class)
			{ CVWidgetKnob } {
				parent.name_("%: MIDI connections".format(widget.name))
			}
			{ CVWidgetMS } {
				parent.name_("%[%]: MIDI connections".format(widget.name, slot))
			}
		}
	}

	front {
		parent.front;
	}

	close {
		all[widget].remove(this);
		e.do(_.close);
	}

	*closeAll {
		all.pairsDo { |key, eds|
			// IMPORTANT
			// with each call to 'close' the index into the list of editors
			// advances by 1. However, as the first call will already have
			// removed the editor at index 0 the next call will not remove
			// the editor at index 1 but the editor at index 2 which has meanwhile
			// become index 1. Hence, every second editor will be omitted if
			// the list of editors isn't reversed before invoking the loop by
			// calling 'do'!
			eds.reverse.do(_.close)
		}
	}
}
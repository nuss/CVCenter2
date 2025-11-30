OscConnectorsEditorView : CompositeView {
	classvar <all;
	var <widget, <parent, <cIndex;
	// GUI elements
	var <e;

	*initClass {
		all = ();
	}

	*new { |widget, connectorID=0, parent|
		^super.new.init(widget, connectorID, parent.asView);
	}

	init { |wdgt, index, parentView|
		var o;

		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = widget.wmc.oscConnectors.m.value.detect { |c| c.name == index }
		};
		if (index.class == OscConnector) {
			index = widget.wmc.oscConnectors.m.value.indexOf(index)
		};

		e = ();

		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		if (parentView.isNil) {
			parent = Window("%: OSC connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };

		if (widget.wmc.oscConnectors.m.value.isEmpty) {
			OscConnector(widget)
		};

		// fallback if index out of bounds
		if (index >= widget.wmc.midiConnectors.m.value.size) {
			index = widget.wmc.midiConnectors.m.value.size - 1;
		};

		e.connectorNameField = ConnectorNameField(parent, widget, connectorID: index, connectorKind: \osc);
		e.connectorSelect = ConnectorSelect(parent, widget, connectorID: index, connectorKind: \osc);
		e.addrAndCmdSelect = OscSelectsComboView(parent, widget, connectorID: index);
		e.oscCmdTextField = OscCmdNameField(parent, widget, connectorID: index);
		e.oscCmdIndexNumBox = OscCmdIndexBox(parent, widget, connectorID: index).value_(widget.wmc.oscDisplay.m.value[index].index);
		e.inputConstraintsLoNumBox = NumberBox(parent).value_(0.1);
		e.inputConstraintsHiNumBox = NumberBox(parent).value_(1.0);
		e.zeroCrossCorrectStaticText = StaticText(parent);
		e.calibrationButton = Button(parent).states_([["calibrate"]]);
		e.resetButton = Button(parent).states_([['reset']]);
		e.specConstraintsStaticText = StaticText(parent).string_("current widget spec constraints (lo/hi): 0/0");
		e.inOutMappingSelect = PopUpMenu(parent).items_(['linlin']);
		e.connectorButton = Button(parent).states_([['connect']]);

		parent.layout_(
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
					[e.oscCmdIndexNumBox]
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
					[e.specConstraintsStaticText],
					[StaticText(parent).string_("input to output mapping")],
					[e.inOutMappingSelect],
					[e.connectorButton]
				)
			)
		);
	}

	set { |connector|
		if (connector.isInteger) {
			connector = widget.wmc.oscConnectors.m.value[connector]
		};
		cIndex = widget.wmc.oscConnectors.m.value.indexOf(connector);
		e.do(_.index_(cIndex));
	}

	front {
		parent.front;
	}
}

MidiConnectorsEditorView : CompositeView {
	classvar <all;
	var <widget, <parent, <index;
	var <connector;
	// GUI elements
	var <e;

	*initClass {
		all = ();
	}

	*new { |widget, connector=0, parent|
		^super.new.init(widget, connector, parent.asView);
	}

	init { |wdgt, index, parentView|
		var m;

		if (wdgt.midiConnectors.isEmpty) {
			MidiConnector(wdgt)
		};

		// index can be an Integer, a Symbol or a MidiConnector instance
		if (index.class == Symbol) {
			index = wdgt.midiConnectors.detect { |c| c.name == index }
		};
		if (index.class == MidiConnector) {
			index = wdgt.midiConnectors.indexOf(index)
		};
		// after all, if index is nil or greater the set it to 0
		if (index.isNil or: { index > wdgt.midiConnectors.lastIndex }) { index = 0 };

		widget = wdgt;
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		if (parentView.isNil) {
			parent = Window("%: MIDI connections".format(widget.name), Rect(0, 0, 300, 375))
		} { parent = parentView };

		parent.onClose_({ this.close });

		e = ();
		e.connectorNameField = ConnectorNameField(parent, widget, connectorID: index, connectorKind: \midi);
		e.connectorSelect = ConnectorSelect(parent, widget, connectorID: index, connectorKind: \midi);
		e.midiModeSelect = MidiModeSelect(parent, widget, connectorID: index);
		e.midiZeroBox = MidiZeroNumberBox(parent, widget, connectorID: index);
		e.snapDistanceBox = SnapDistanceNumberBox(parent, widget, connectorID: index);
		e.midiResolutionBox = MidiResolutionNumberBox(parent, widget, connectorID: index);
		e.slidersPerGroupNB = SlidersPerGroupNumberBox(parent, widget, connectorID: index);
		e.midiLearnButton = MidiLearnButton(parent, widget, connectorID: index);
		e.midiSrcSelect = MidiSrcSelect(parent, widget, connectorID: index);
		e.midiChanTF = MidiChanField(parent, widget, connectorID: index);
		e.midiNumTF = MidiCtrlField(parent, widget, connectorID: index);
		e.mappingSelect = MappingSelect(parent, widget, connectorID: index, connectorKind: \midi);
		e.midiInit = MidiInitButton(parent);
		e.midiConnectorRemove = MidiConnectorRemoveButton(parent, widget, connectorID: index);

		parent.layout_(
			VLayout(
				HLayout(
					[e.connectorNameField, stretch: 9],
					[e.connectorSelect, stretch: 1]
				),
				HLayout(
					[StaticText(parent).string_("MIDI mode: 0-127 or endless "), stretch: 7],
					[e.midiModeSelect, stretch: 3]
				),
				HLayout(
					[StaticText(parent).string_("MIDI mean (endless mode only): "), stretch: 7],
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
					[e.midiSrcSelect, stretch: 4],
					[e.midiChanTF, stretch: 4],
					[e.midiNumTF, stretch: 4]
				),
				HLayout(
					[e.midiInit],
					[e.midiConnectorRemove]
				)
			)
		);

		e.connectorSelect.view.action_({ |sel|
			if (sel.value == (sel.items.size - 1)) {
				m = widget.addMidiConnector;
				e.connectorSelect.view.value_(widget.wmc.midiConnectors.m.value.indexOf(m));
			};

			if (sel.value < (sel.items.size - 1)) {
				e.do(_.index_(sel.value));
				// enable or disable selects for MIDI source, channel and ctrl number based on connection status
				[e.midiSrcSelect, e.midiChanTF, e.midiNumTF].do { |elem|
					elem.view.enabled_(widget.wmc.midiConnections.m[sel.value].value.isNil)
				}
			}
		})
	}

	set { |connector|
		if (connector.isInteger) {
			index = connector
		} {
			index = widget.wmc.midiConnectors.m.value.indexOf(connector)
		};
		e.do(_.index_(index));
	}

	widget_ { |otherWidget|
		// FIXME: check for CVWidget2D slot (once it's implemented...)
		if (otherWidget.class !== CVWidgetKnob) {
			Error("Widget must be a CVWidgetKnob").throw
		};

		all[widget].remove(this);
		widget = otherWidget;
		connector = widget.wmc.midiConnectors.m.value[0];
		all[widget] ?? { all[widget] = List[] };
		if (all[widget].includes(this).not) { all[widget].add(this) };
		e.do(_.widget_(widget));
		if (parent.class === Window) {
			parent.name_("%: MIDI connections".format(widget.name))
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
			// VERY IMPORTANT
			// with each call to 'close' the index into the list of editors
			// advances by 1. However, as the first call will already have
			// removed the editor at index 0 the next call will not remove
			// the editor at index 1 but the editor at index 2 which has meanwhile
			// become index 1. Hence, every second editor will be omitted if
			// the list of editors isn't reversed before invoking the loop by
			// calling 'do'!!!
			eds.reverse.do(_.close)
		}
	}
}
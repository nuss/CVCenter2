OscConnectorsEditorView : CompositeView {
	var <widget, <mc, <parent, <cIndex;
	// GUI elements
	var connectionSelect, addButton, removeButton;
	var ipSelect, restrictToPortCheckBox;
	var deviceSelect, oscCmdSelect, newDeviceBut;
	var oscCmdTextField, oscCmdSlotNumBox;
	var inputConstraintsLoNumBox, inputConstraintsHiNumBox, zeroCrossCorrectStaticText;
	var calibrationButton, resetButton;
	var specConstraintsStaticText, inOutMappingSelect, connectionButton;
	// var more, more...

	// FIXME: an OscConnector is a single connection
	// yet, a selection of connections can only belong
	// to the widget
	// -> must be considered when adding the model to the widget's
	// model - maybe the model can't be kept with the view?
	*new { |widget, parent|
		^super.new.init(widget, parent.asView);
	}

	init { |wdgt, parentView|
		widget = wdgt;
		if (parentView.isNil) {
			parent = Window("%: OSC connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };
		// Just create a new OscConnector if none exists
		// it will automatically be added to the widget's list
		// of OscConnectors within OscConnector:-init - should
		// also update the connectionSelect's items
		if (widget.oscConnectors.isEmpty) {
			OscConnector(widget)
		};
		mc = widget.oscConnectors;
		parent.layout_(
			VLayout(
				HLayout(
					connectionSelect = PopUpMenu(parent),
					addButton = Button(parent).states_([["+"]]),
					removeButton = Button(parent).states_([["-"]])
				),
				HLayout(
					ipSelect = PopUpMenu(parent),
					StaticText(parent).string_("restrict to port"),
					restrictToPortCheckBox = CheckBox(parent)
				),
				StaticText(parent).string_("OSC command name - either select from list provided by the selected device or set custom one"),
				HLayout(
					deviceSelect = PopUpMenu(parent),
					oscCmdSelect = PopUpMenu(parent),
					newDeviceBut = Button(parent)
				),
				HLayout(
					oscCmdTextField = TextField(parent),
					oscCmdSlotNumBox = NumberBox(parent)
				),
				StaticText(parent).string_("OSC input constraints, zero-crossing correction"),
				HLayout(
					inputConstraintsLoNumBox = NumberBox(parent),
					inputConstraintsHiNumBox = NumberBox(parent),
					zeroCrossCorrectStaticText = StaticText(parent),
					calibrationButton = Button(parent),
					resetButton = Button(parent),
				),
				specConstraintsStaticText = StaticText(parent).string_("current widget spec constraints (lo/hi): 0/0"),
				StaticText(parent).string_("input to output mapping"),
				inOutMappingSelect = PopUpMenu(parent),
				connectionButton = Button(parent)
			)
		);
	}

	set { |connection|
		if (connection.class == Symbol) {
			connection = widget.oscConnectors.detect { |c| c.name == connection }
		};
		if (connection.isInteger) {
			connection = widget.oscConnectors[connection]
		};
		cIndex = widget.oscConnectors.indexOf(connection);
		[
			connectionSelect, addButton, removeButton,
			ipSelect, restrictToPortCheckBox, deviceSelect,
			oscCmdSelect, newDeviceBut, oscCmdTextField,
			oscCmdSlotNumBox, inputConstraintsLoNumBox,
			inputConstraintsHiNumBox, zeroCrossCorrectStaticText,
			calibrationButton, resetButton, specConstraintsStaticText,
			inOutMappingSelect, connectionButton
		].do(_.set(cIndex));
	}

	front {
		parent.front;
	}
}

MidiConnectorsEditorView : CompositeView {
	classvar <allEditorViews;
	var <widget, <parent, <index;
	// GUI elements
	var <connectionSelect;
	var midiModeSelect, midiMeanBox, softWithinBox, midiResolutionBox, slidersPerBankTF;
	var midiLearnButton, midiSrcSelect, midiChanTF, midiNumTF;

	*initClass {
		allEditorViews = ();
	}

	*new { |widget, connector=0, parent|
		^super.new.init(widget, connector, parent.asView);
	}

	init { |wdgt, connector, parentView|
		var mc, cv;

		index = case
		{ connector.class == Symbol } {
			widget.midiConnectors.detectIndex { |c| c.name == connector }
		}
		{ connector.class == MidiConnector } {
			widget.midiConnectors.indexOf(connector)
		}
		{ connector };

		widget = wdgt;

		if (allEditorViews[widget].isNil) {
			allEditorViews[widget] = List[this]
		} {
			allEditorViews[widget].add(this)
		};

		if (parentView.isNil) {
			parent = Window("%: MIDI connections".format(widget.name), Rect(0, 0, 300, 300))
		} { parent = parentView };
		if (widget.midiConnectors.isEmpty) {
			MidiConnector(widget)
		};

		mc = widget.wmc;

		parent.layout_(
			VLayout(
				HLayout(
					connectionSelect = MidiConnectorSelect(parent, widget)
					.action_({ |cs|
						this.set(cs.value - 1)
					})
				),
				HLayout(
					StaticText(parent).string_("MIDI mode: 0-127 or in/decremental "),
					midiModeSelect = MidiModeSelect(parent, widget, index)
				),
				HLayout(
					StaticText(parent).string_("MIDI mean (in/decremental mode only): "),
					midiMeanBox = MidiMeanNumberBox(parent, widget, index)
				),
				HLayout(
					StaticText(parent).string_("min. snap distance for slider (0-127 only): "),
					softWithinBox = SoftWithinNumberBox(parent, widget, index)
				),
				HLayout(
					StaticText(parent).string_("MIDI resolution (+/- only): "),
					midiResolutionBox = MidiResolutionNumberBox(parent, widget, index)
				),
				HLayout(
					StaticText(parent).string_("Number of sliders per bank: "),
					slidersPerBankTF = SlidersPerBankNumberTF(parent, widget, index)
				),
				HLayout(
					StaticText(parent).string_("Yaddayadda")
				),
				HLayout(
					midiLearnButton = MidiLearnButton(parent, widget, index),
					midiSrcSelect = MidiSrcSelect(parent, widget, index),
					midiChanTF = MidiChanField(parent, widget, index),
					midiNumTF = MidiCtrlField(parent, widget, index)
				)
			)
		);

		this.initControllers(mc);
	}

	initControllers { |mc, cv|
		mc.midiDisplay.controller.put(\editorView, { |changer, what ... more|
			var vals = changer[index].value;
			vals.learn !? {
				midiLearnButton.value_(midiLearnButton.states.indexOf(vals.learn))
			};
			vals.src !? {
				midiSrcSelect.items.indexOf(vals.src) !? {
					midiSrcSelect.value_(midiSrcSelect.items.indexOf(vals.src))
				}
			};
			vals.chan !? {
				midiChanTF.string_(vals.chan)
			};
			vals.ctrl !? {
				midiNumTF.string_(vals.ctrl)
			}
		});
		mc.midiOptions.controller.put(\editorView, { |changer, what ... more|
			var vals = changer[index].value;
			vals.midiMode !? {
				midiModeSelect.value_(vals.midiMode)
			};
			vals.midiMean !? {
				midiMeanBox.value_(vals.midiMean)
			};
			vals.softWithin !? {
				softWithinBox.value_(vals.softWithin)
			};
			slidersPerBankTF.string_(vals.ctrlButtonBank);
			vals.midiResolution !? {
				midiResolutionBox.value_(vals.midiResolution)
			}
		});
		widget.prAddSyncKey(\editorView, true);
	}

	set { |connector|
		if (connector.class == Symbol) {
			connector = widget.midiConnectors.detect { |c| c.name == connector }
		};
		if (connector.isInteger) {
			connector = widget.midiConnectors[connector]
		};
		index = widget.midiConnectors.indexOf(connector);
		[midiModeSelect, midiMeanBox, softWithinBox, midiResolutionBox, slidersPerBankTF].do(_.set(index));
	}

	front {
		parent.front;
	}


}
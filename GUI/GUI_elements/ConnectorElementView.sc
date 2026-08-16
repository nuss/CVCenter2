ConnectorElementView : SCViewHolder {
	// the widget, the slot - if widget is a CVWidgetMS, otherwise nil
	// initialized through super.newCopyArgs
	var <widget, <slot;
	// the connector (not the ID but the object)
	var <connector;
	// TODO: should be local
	var mc, conModel;
	// models, controllers, differentiated by their widget's class'
	var mcM, mcC;
	var conModelM, conModelC;

	var namesM, namesC;
	var optionsM, optionsC;
	var displayM, displayC;
	var connectors, connectorsM, connectorsC;
	var connectionsM, connectionsC;

	// specific to every element
	var syncKey;
	// could be local but doesn't have to
	var toolTip;

	close {
		this.remove;
		this.viewDidClose;
		this.prCleanup;
	}

	prRemoveControllers {
		[namesC, optionsC, displayC, connectorsC, connectionsC].do(_.removeAt(syncKey))
	}

	prCleanup {
		this.class.all[this.widget].remove(this);

		// TODO: take care of CVWidgetKnob and CVWidgetMS separation

		if (this.class.all[this.widget].isEmpty) {
			this.prRemoveControllers;
			this.widget.prRemoveSyncKey(syncKey, true);
		}
	}

	prMCDistinct { |connectorKind|
		// models and controllers
		var names, options, display, connections;

		connectorKind ?? {
			Error("ConnectorElementView:-prMCDistinct expects a connector type: 'osc' or 'midi'").throw;
		};

		switch (connectorKind)
		{ \midi } {
			names = this.widget.wmc.midiConnectorNames;
			options = this.widget.wmc.midiOptions;
			display = this.widget.wmc.midiDisplay;
			connectors = this.widget.wmc.midiConnectors;
			connections = this.widget.wmc.midiConnections;
		}
		{ \osc } {
			names = this.widget.wmc.oscConnectorNames;
			options = this.widget.wmc.oscOptions;
			display = this.widget.wmc.oscDisplay;
			connectors = this.widget.wmc.oscConnectors;
			connections = this.widget.wmc.oscConnections;
		};

		case
		{ this.widget.class === CVWidgetKnob } {
			namesM = names.m;
			namesC = names.c;
			optionsM = options.m;
			optionsC = options.c;
			displayM = display.m;
			displayC = display.c;
			connectorsM = connectors.m;
			connectorsC = connectors.c;
			connectionsM = connections.m;
			connectionsC = connections.c;
			connectors = connectors.m.value;
		}
		{ this.widget.class === CVWidgetMS } {
			namesM = names.m[this.slot];
			namesC = names.c[this.slot];
			optionsM = options.m[this.slot];
			optionsC = options.c[this.slot];
			displayM = display.m[this.slot];
			displayC = display.c[this.slot];
			connectorsM = connectors.m[this.slot];
			connectorsC = connectors.c[this.slot];
			connectionsM = connections.m[this.slot];
			connectionsC = connections.c[this.slot];
			connectors = connectors.m[this.slot].value;
		}
	}

	// suitable for all instances, hence widget must
	// be passed in explicitly within Osc/MidiConnector:-remove
	prOnRemoveConnector { |widget, index|
		if (index > 0) {
			this.class.all[widget].do(_.index_(index - 1))
		} {
			this.class.all[widget].do(_.index_(index))
		}
	}

	slot_ { |newSlot|
		if (this.class === CVWidgetMS) { slot = newSlot }
	}
}

ConnectorNameField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	// connectorKind - either \midi or \osc
	// initialized through super.newCopyArgs
	var connectorKind, cclass;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0), connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		// ^super.new.init(parent, widget, rect, connectorID, connectorKind)
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	// init { |parentView, wdgt, rect, index, kind|
	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);
		this.prMCDistinct(connectorKind);
		this.view = TextField(parentView, rect);
		this.index_(index);
		this.view.action_({ |tf|
			// this.connector - an OscConnector or a MidiConnector set in index_
			this.connector.name_(tf.string.asSymbol)
		});
		this.view.onClose_({ this.close });
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			// FIXME: if widget is a CVWidgetMS likely a slot index must be given
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = connectors[connectorID];
		namesM.value !? {
			this.view.string_(namesM.value[connectorID])
		}
	}

	// setWidget { |otherWidget, slot|
	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(connectorKind);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;
		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		// if there's already a function defined for synKey simply replace it
		namesC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |tf|
				if (tf.connector === connectors[conID]) {
					tf.view.string_(changer.value[conID]);
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				this.prRemoveControllers;
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}

}

ConnectorSelect : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var cons, connectorKind, cclass;
	var consM, consC;
	var selItem0;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0), connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		// ^super.new.init(parent, widget, rect, connectorID, connectorKind);
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		case
		{ connectorKind === \midi } {
			selItem0 = 'add MidiConnector...'
		}
		{ connectorKind === \osc } {
			selItem0 = 'add OscConnector...'
		};

		this.prMCDistinct(connectorKind);
		this.view = PopUpMenu(parentView)
		.items_(mcM.value ++ [selItem0]);
		this.view.onClose_({ this.close });
		this.index_(index);
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = connectors[connectorID];
		this.view.value_(connectorID);
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(connectorKind);
		this.view.items_(mcM.value ++ this.view.items.last);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var items, conID;
		var curValue;

		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		connectorsC.put(syncKey, { |changer, what ... moreArgs|
			all[widget][connectorKind].do { |sel, i|
				curValue = sel.view.value;
				sel.view.items_(namesC.value ++ sel.view.items.last)
				.value_(curValue);
			}
		});
		namesC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |sel, i|
				items = sel.view.items;
				items[conID] = changer.value[conID];
				curValue = sel.view.value;
				sel.view.items_(items).value_(curValue);
				if (sel.connector === connectors[conID]) {
					sel.view.value_(conID)
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				this.prRemoveControllers;
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

ConnectorRemoveButton : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var connectorKind, cclass;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID(0), connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		// ^super.new.init(parent, widget, rect, connectorID, connectorKind);
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	init { |parentView, rect, index|
		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);
		this.prMCDistinct(connectorKind);
		this.index_(index);
		this.view = Button(parentView, rect)
		.states_([["remove Connector", Color.white, Color(0, 0.5, 0.5)]])
		.action_({ this.connector.remove });
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		}
	}

	index_ { |connectorID|
		connector = connectors[connectorID];
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);
		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(connectorKind);
		this.index_(0);
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

// displays current ControlSpec,
// independent from MIDI and OSC,
// resp., current connector
ControlSpecText : ConnectorElementView {
	classvar <all, mc;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		^super.newCopyArgs(widget: widget).init(parent, rect)
	}

	init { |parentView, rect|
		all[widget] ?? { all[widget] = List[] };
		all[widget].add(this);

		mc = widget.wmc.cvSpec;

		this.view = StaticText(parentView, rect)
		.string_("Current ControlSpec:\n%".format(mc.m.value));
		this.view.onClose_({ this.close });
		this.prAddController;
	}

	index_ {}

	setWidget { |otherWidget|
		all[otherWidget] ?? { all[otherWidget] = List[] };
		all[otherWidget].add(this);
		this.prCleanup;
		mc = widget.wmc.cvSpec;
		this.prAddController;
	}

	prAddController {
		syncKey = this.class.asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		mc.c.put(syncKey, { |changer, what ... moreArgs|
			all[widget].do { |txt|
				defer { txt.string_("Current ControlSpec:\n%".format(changer.value)) }
			}
		})
	}

	prCleanup {
		all[this.widget].remove(this);
		if (all[this.widget].isEmpty) {
			mc.c.removeAt(syncKey);
			this.widget.prRemoveSyncKey(syncKey, true);
		}
	}
}

TemplateTextField : ConnectorElementView {
	classvar <all, connectorRemovedFuncAdded;
	var connectorKind;
	var cclass, connections;
	var connectionsM, connectionsC;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, slot, connectorID=0, connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	init { |parentView, rect, index|
		var conID, action;

		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		this.prMCDistinct(connectorKind);
		this.view = TextView(parentView)
		.string_(displayM.value[index].template.cs)
		.syntaxColorize
		.font_(Font.monospace);
		this.view.onClose_({ this.close });
		this.index_(index);
		action = { |tv|
			conID = connector.index;
			if (tv.string.size > 0) {
				displayM.value[conID].template = tv.string.interpret;
			} {
				displayM.value[conID].template = nil;
			};
			displayM.changedPerformKeys(widget.syncKeys, conID);
		};
		this.view.action_(action);
		this.view.focusLostAction_(action);
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.prAddController;
	}

	index_ { |connectorID|
		connector = connectors[connectorID];
		this.view.string_(displayM.value[connectorID].template)
		.editable_(connectionsM.value[connectorID].isNil);
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);
		this.prMCDistinct(connectorKind);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;

		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		displayC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |tv|
				if (tv.connector === connectors[conID]) {
					defer { tv.view.string_(displayM.value[conID].template) }
				}
			}
		});
		connectionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |tv|
				if (tv.connector === connectors[conID]) {
					defer { tv.view.enabled_(connectionsM.value[conID].isNil) }
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				this.prRemoveControllers;
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

PlayPauseButton : ConnectorElementView {
	// inspired by https://scsynth.org/t/is-it-possible-to-make-a-round-button/7082/3
	classvar <all, connectorRemovedFuncAdded;
	var connectorKind;
	var cclass, <buttonLayout;
	var disabledBgColor, disabledFgColor;
	var enabledFgColor;
	var enabledBgColor; // array [0 = paused, 1 = playing]
	var funcClassName, enabledMethod, toolTips;

	*initClass {
		all = ()
	}

	*new { |parent, widget, rect, slot, connectorID=0, connectorKind|
		if (widget.isKindOf(CVWidget).not) {
			Error("arg 'widget' must be a kind of CVWidget").throw
		};
		if (connectorKind.isNil) {
			Error("arg 'connectorKind' in ConnectorNameField.new must not be nil - must either be 'midi' or 'osc'.").throw
		} {
			connectorKind = connectorKind.asSymbol;
			if (connectorKind !== \midi and: { connectorKind !== \osc }) {
				Error("arg 'connectorKind' must be a String or Symbol, either 'midi' or 'osc'. Given: %".format(connectorKind)).throw
			}
		};
		^super.newCopyArgs(widget: widget, slot: slot, connectorKind: connectorKind).init(parent, rect, connectorID)
	}

	init { |parentView, rect, index|
		var conID, action, buttonBgColor ;

		all[widget] ?? { all[widget] = () };
		all[widget][connectorKind] ?? {
			all[widget][connectorKind] = List[]
		};
		all[widget][connectorKind].add(this);

		disabledBgColor = Color.gray(0.6);
		disabledFgColor = Color.gray(0.9);
		enabledBgColor = [Color.green, Color.red];
		enabledFgColor = Color.black;

		case
		{ connectorKind === \midi } {
			funcClassName = "MIDIFunc";
			enabledMethod = \getMIDIFuncEnabled
		}
		{ connectorKind === \osc } {
			funcClassName = "OSCFunc";
			enabledMethod = \getOSCFuncEnabled
		};

		this.prMCDistinct(connectorKind);

		toolTips = [
			"Click to disable %".format(funcClassName),
			"Click to enable %".format(funcClassName)
		];

		buttonBgColor = if (connectionsM.value[index].notNil) {
			enabledBgColor[connectionsM.value[index].enabled.asInteger]
		} { Color.gray(0.6) };

		this.view = Button(parentView);
		this.view.states_([
			["", Color.clear, Color.clear],
			["", Color.clear, Color.clear]
		])
		.canFocus_(false)
		.layout_(
			HLayout(
				buttonLayout = UserView()
				.acceptsMouse_(false)
				.background_(buttonBgColor)
				.drawFunc_(this.prMakeLabelDrawFunc(
					connectionsM.value[index].notNil,
					connectionsM.value[index] !? { connectionsM.value[index].enabled }
				))
			)
			.margins_(0)
			.spacing_(0)
		)
		.enabled_(connectionsM.value[index].notNil)
		.action_({ |bt|
			if (connectorKind === \midi) {
				this.connector.setMIDIFuncEnabled(bt.value.asBoolean.not);
			} {
				this.connector.setOSCFuncEnabled(bt.value.asBoolean.not)
			};
			bt.toolTip_(toolTips[bt.value])
		})
		.maxWidth_(25);
		connectorRemovedFuncAdded ?? {
			case
			{ connectorKind === \midi } { cclass = MidiConnector }
			{ connectorKind === \osc } { cclass = OscConnector };
			cclass.onConnectorRemove_({ |widget, id|
				this.prOnRemoveConnector(widget, id, connectorKind)
			});
			connectorRemovedFuncAdded = true
		};
		this.index_(index);
		if (connectionsM.value[index].notNil) {
			this.view.toolTip_(toolTips[connector.perform(enabledMethod).not.asInteger])
		} {
			this.view.toolTip_("No % currently present".format(funcClassName))
		};
		this.prAddController;
	}

	prMakeLabelDrawFunc { |funcExists, enabled = false|
		var fgColor, bgColor, funcEnabled;
		var iconSize;

		if (this.view.bounds.width > this.view.bounds.height) {
			iconSize = this.view.bounds.height/2
		} {
			iconSize = this.view.bounds.width/2
		};

		if (funcExists) {
			fgColor = enabledFgColor;
			bgColor = enabledBgColor[enabled.asInteger]
		} {
			fgColor = disabledFgColor;
			bgColor = disabledBgColor;
		};

		case
		{ funcExists and: { enabled.not }} {
			^{ |v|
				Pen
				.fillColor_(fgColor)
				.moveTo(Point(this.view.bounds.width/2-(iconSize/2), this.view.bounds.height/2-(iconSize/2)))
				.lineTo(Point(this.view.bounds.width/2+(iconSize/2), this.view.bounds.height/2))
				.lineTo(Point(this.view.bounds.width/2-(iconSize/2), this.view.bounds.height/2+(iconSize/2)))
				.fill
			}
		}
		{ (funcExists.not).or(funcExists and: { enabled }) } {
			^{ |v|
				Pen
				.fillColor_(fgColor)
				.addRect(Rect(
					this.view.bounds.width/2-(iconSize/2),
					this.view.bounds.height/2-(iconSize/2),
					iconSize/5*2,
					iconSize
				))
				.addRect(Rect(
					this.view.bounds.width/2-(iconSize/2)+(iconSize/5*3),
					this.view.bounds.height/2-(iconSize/2),
					iconSize/5*2,
					iconSize
				)).fill
			}
		}
	}

	index_ { |connectorID|
		connector = connectors[connectorID];
		if (connectionsM.value[connectorID].notNil) {
			buttonLayout.background_(enabledBgColor[connectionsM.value[connectorID].enabled.asInteger])
			.drawFunc_(this.prMakeLabelDrawFunc(true, connectionsM.value[connectorID].enabled)).refresh;
			this.view.enabled_(true);
		} {
			buttonLayout.background_(Color.gray(0.6))
			.drawFunc_(this.prMakeLabelDrawFunc(false)).refresh;
			this.view.enabled_(false);
		}
	}

	setWidget { |otherWidget, slot|
		all[otherWidget] ?? { all[otherWidget] = () };
		all[otherWidget][connectorKind] ?? {
			all[otherWidget][connectorKind] = List[]
		};
		all[otherWidget][connectorKind].add(this);

		this.prCleanup;
		// switch after cleanup has finished
		widget = otherWidget;
		this.slot_(slot);

		this.prMCDistinct(connectorKind);
		// midiConnector at index 0 should always exist (who knows...)
		this.index_(0);
		this.prAddController;
	}

	prAddController {
		var conID;
		var funcEnabled;

		syncKey = (connectorKind ++ this.class.asString).asSymbol;
		widget.syncKeys.indexOf(syncKey) ?? {
			widget.prAddSyncKey(syncKey, true)
		};
		connectionsC.put(syncKey, { |changer, what ... moreArgs|
			conID = moreArgs[0];
			all[widget][connectorKind].do { |bt|
				if (bt.connector === connectors[conID]) {
					switch (connectorKind)
					{ \midi } { funcEnabled = connectors[conID].getMIDIFuncEnabled }
					{ \osc } { funcEnabled = connectors[conID].getOSCFuncEnabled };
					if (connectionsM.value[conID].notNil) {
						defer {
							bt.buttonLayout
							.background_(enabledBgColor[funcEnabled.asInteger])
							.drawFunc_(bt.prMakeLabelDrawFunc(true, funcEnabled)).refresh;
							bt.enabled_(true).toolTip_(toolTips[bt.connector.perform(enabledMethod).not.asInteger])
						}
					} {
						defer {
							bt.buttonLayout
							.background_(disabledBgColor)
							.drawFunc_(bt.prMakeLabelDrawFunc(false)).refresh;
							bt.enabled_(false).toolTip_("No % currently present".format(funcClassName))
						}
					}
				}
			}
		})
	}

	prCleanup {
		all[widget][connectorKind].remove(this);
		try {
			if (all[widget][connectorKind].notNil and: { all[widget][connectorKind].isEmpty }) {
				this.prRemoveControllers;
				widget.prRemoveSyncKey(syncKey, true);
				all[widget].removeAt(connectorKind);
			}
		}
	}

	prOnRemoveConnector { |widget, index, connectorKind|
		// if widget has already been removed let it fail
		try {
			if (index > 0) {
				all[widget][connectorKind].do(_.index_(index - 1))
			} {
				all[widget][connectorKind].do(_.index_(index))
			}
		}
	}
}

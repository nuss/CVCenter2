MappingSelect : CompositeView {
	classvar all;
	var mc, connectors;
	var <connector, <widget;
	var e;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, connectorKind, layout([[\mselect, \mcurve, \mplot], [\menv]])|
		^super.new.init(parent, widget, rect, connectorID, connectorKind, layout);
	}

	init { |parentView, wdgt, rect, index, connectorKind, layout|
		var parent, row;
		var ramp;

		all[wdgt] ?? {
			all.put(wdgt, List[])
		};
		all[wdgt].add(this);

		widget = wdgt;

		connectorKind ?? {
			Error("arg connectorKind in MappingSelect.new must either be 'midi' or 'osc'!").throw
		};
		connectorKind = connectorKind.asSymbol;
		case
		{ connectorKind === \midi } {
			mc = widget.wmc.midiInputMappings;
			connectors = widget.midiConnectors;
		}
		{ connectorKind === \osc } {
			mc = widget.wmc.oscInputMappings;
			connectors = widget.oscConnectors;
		};

		// the kind of connector must be known by now, so, index_ should already know

		if (parentView.isNil) {
			parent = Window("%: % mappings".format(widget.name, connectorKind.asString.toUpper), Rect(0, 0, 300, 65)).front
		} { parent = parentView };

		ramp = switch (mc.model.value[index].mapping)
		{ \linenv } { mc.model.value[index].env }
		{ \lincurve } { [\lincurve, mc.model.value[index].curve] }
		{ \linbicurve } { [\linbicurve, mc.model.value[index].curve] }
		{ mc.model.value[index].mapping };

		e = ();
		e.mplot = RampPlot(parent, ramp: ramp);
		e.mselect = PopUpMenu(parent).items_([
			"linlin", "linexp", "explin", "expexp", "lincurve", "linbicurve", "linenv"
		]);
		e.mcurve = NumberBox(parent).clipHi_(12).clipLo_(-12);
		e.menv = TextField(parent)
		.string_((mc.model.value[index].env ? Env([0, 1], [1])).asCompileString);

		if (layout.size > 1) {
			parent.layout_(VLayout());
			layout.size.do { |i|
				row = HLayout();
				layout[i].do { |k| row.add(e[k]) };
				parent.layout.add(row)
			};
		} {
			row = HLayout();
			layout[0].do { |k| row.add(e[k]) };
			parent.layout.add(row)
		};

		this.index_(index);

		e.mselect.action_({ |sel|
			var i = connectors.indexOf(this.connector);
			case
			{ sel.value == 4 or: { sel.value == 5 }} {
				mc.model.value[i].mapping = sel.items[sel.value].asSymbol;
				mc.model.value[i].curve = e.mcurve.value;
				mc.model.value[i].env = nil;
			}
			{ sel.value == 6 } {
				try {
					mc.model.value[i].env = e.menv.string.interpret;
					mc.model.value[i].mapping = sel.items[sel.value].asSymbol;
					mc.model.value[i].curve = nil;
				} { |err|
					"The given string doesn't compile to a valid Env: % (%)".format(e.menv.string, err).error
				}
			}
			{
				mc.model.value[i].mapping = sel.items[sel.value].asSymbol;
				mc.model.value[i].env = nil;
				mc.model.value[i].curve = nil;
			};
			mc.model.value.changedPerformKeys(widget.syncKeys, i);
			mc.model.value[i].postln;
		});
		e.mcurve.action_({ |nb|
			var i = connectors.indexOf(this.connector);
			if (e.mselect.value == 4 or: e.mselect.value == 5) {
				mc.model.value[i].curve = e.mcurve.value;
			};
			mc.model.value.changedPerformKeys(widget.syncKeys)
		});
		e.menv.action_({ |tf|
			var i = connectors.indexOf(this.connector);
			if (e.mselect.value == 6) {
				try {
					mc.model.value[i].env = e.menv.string.interpret
				}  { |err|
					"The given string doesn't compile to a valid Env: % (%)".format(e.menv.string, err).error
				};
				mc.model.value.changedPerformKeys(widget.syncKeys)
			}
		});
	}

	index_ { |connectorID|
		var modelVal;

		connector = connectors[connectorID];
		modelVal = mc.model.value[connectorID];
		modelVal !? {
			e.mselect.value_(connectorID);
			case
			{ modelVal.mapping === \lincurve or: { modelVal.mapping === \linbicurve }} {
				e.mplot.draw([modelVal.mapping, modelVal.curve]);
				e.mcurve.value_(modelVal.mapping[1]).enabled_(true);
				e.menv.string_((modelVal.env ? Env([0, 1], [1])).asCompileString).enabled_(false);
			}
			{ modelVal.mapping === \linenv and: { modelVal.env.class === Env }} {
				e.mplot.draw(modelVal.env);
				e.mcurve.value_(0).enabled_(false);
				e.menv.string_(modelVal[1].asCompileString).enabled_(true);
			}
			{
				modelVal.mapping !== \linenv and: {
					modelVal.mapping !== \lincurve and: {
						modelVal.mapping !== \linbicurve
					}
				}
			} {
				e.mplot.draw(modelVal.mapping);
				e.mcurve.postln.value_(0).enabled_(false);
				e.menv.string_((modelVal.env ? Env([0, 1], [1])).asCompileString).enabled_(false);
			}
		}
	}

	prAddController {}

	close {}

}

RampPlot : SCViewHolder {
	var <>background, <>foreground;

	*new { |parent, rect, ramp = \linlin, background(Color.blue(0.3)), foreground(Color.cyan)|
		^super.newCopyArgs(nil, background, foreground).init(parent, rect, ramp)
	}

	init { |parentView, rect, ramp|
		this.view = UserView(parentView, rect)
		.background_(this.background)
		.minWidth_(40)
		.minHeight_(25);
		this.draw(ramp);
	}

	draw { |ramp|
		var rampVals = this.prCreateRampVals(ramp);

		this.view.background_(this.background);
		this.view.drawFunc_({ |v|
			Pen.strokeColor_(this.foreground).width_(2).moveTo(0@this.view.bounds.height);
			(rampVals.size - 1).do { |i|
				Pen.lineTo(Point(
					this.view.bounds.width/rampVals.size*(i+1),
					this.view.bounds.height-(rampVals[i+1] * this.view.bounds.height)
				))
			};
			Pen.stroke;
		});
		this.view.refresh
	}

	prCreateRampVals { |ramp|
		// rampArray models a linear ramp;
		var rampArray = (0, 0.01..1);

		switch (ramp.class)
		{ Array } {
			if (ramp.size < 2) {
				"if arg 'ramp' is given as an Array it must contain two values: the first one denoting the mapping method (\\lincurve or \\linbicurve) and the second one a number denoting the curve caracteristics".error;
				^rampArray;
			} {
				ramp[0] = ramp[0].asSymbol;
				if (ramp[1].isNumber) {
					switch (ramp[0])
					{ \lincurve } {
						^rampArray.perform(ramp[0], 0, 1, 0, 1, ramp[1])
					}
					{ \linbicurve } {
						^rampArray.perform(ramp[0], 0, 1, nil, 0, 1, nil, ramp[1])
					}
				}
			}
		}
		{ Env } {
			if (ramp.respondsTo(\asMultichannelSignal).not) {
				"No valid envelope given - defaulting to Env([0, 1], [1], \lin)".error;
				^rampArray
			} {
				^rampArray.linenv(0, 1, 0, 1, ramp, \minmax, rampArray.size)
			}
		}
		{ ^rampArray.perform(ramp.asSymbol, 0.02, 1, 0.02, 1) }
	}

}
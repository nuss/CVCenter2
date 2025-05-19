MappingSelect : CompositeView {
	classvar all;
	var mc;
	var <connector, <widget;
	var e;

	*initClass {
		all = ();
	}

	*new { |parent, widget, rect, connectorID=0, connectorKind, layout([[\plot, \mselect, \curveNum], [\env]])|
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
		mc = case
		{ connectorKind === \midi } { widget.wmc.midiInputMappings }
		{ connectorKind === \osc } { widget.wmc.oscInputMappings };

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
		e.plot = RampPlot(parent, 45@30, ramp);
		e.mselect = PopUpMenu(parent).items_([
			"linlin", "linexp", "explin", "expexp", "lincurve", "linbicurve", "linenv"
		]);
		e.curveNum = NumberBox(parent, 40@30);
		e.env = TextField(parent);

		if (layout.size > 1) {
			parent.layout_(VLayout());
			layout.size.do { |i|
				row = HLayout();
				layout[i].do { |k| row.add(e[k]) };
				parent.layout.add(row)
			}
		} {
			row = HLayout();
			layout[0].do { |k| row.add(e[k]) };
			parent.layout.add(row)
		};

		this.index_(index);
	}

	index_ { |connectorID|
		var modelVal;

		connector = widget.midiConnectors[connectorID];
		modelVal = mc.model.value[connectorID];
		modelVal !? {
			e.mselect.value_(connectorID);
			if (modelVal.isArray) {
				case
				{ modelVal.mapping[1].isNumber } {
					e.plot.draw(modelVal.mapping[0]);
					e.curveNum.value_(modelVal.mapping[1]).enabled_(true);
					e.env.string_("").enabled_(false);
				}
				{ modelVal.mapping[1].class == Env } {
					e.plot.draw(modelVal[1]);
					e.curveNum.value_(0).enabled_(false);
					e.env.string_(modelVal[1].asCompileString).enabled_(true);
				}
			} {
				e.plot.draw(modelVal.mapping);
				e.curveNum.value_(0).enabled_(false);
				e.env.string_("").enabled_(false);
			}
		}
	}

	prAddController {}

	close {}

}
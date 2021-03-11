package com.lexcorp.joura.templates;

import com.lexcorp.joura.listeners.FiledChangeListener;

import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtMethod;
import spoon.template.BlockTemplate;
import spoon.template.Parameter;

public class TrackableTemplate extends BlockTemplate {
    public TrackableTemplate(CtBlock _original_) {
        this._original_ = _original_;
    }

    @Parameter
    CtBlock _original_;

//    @Parameter
//    List<CtField<?>> _fields_;

    @Override
    public void block() {
        _original_.S();
        System.out.println("HELLO");
        FiledChangeListener.getInstance().accept((CtMethod<?>) _original_.getParent(), null);
    }


}

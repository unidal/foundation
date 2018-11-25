/* THIS FILE WAS AUTO GENERATED BY codegen-maven-plugin, DO NOT EDIT IT */
package org.unidal.mixin.model.transform;

import org.unidal.mixin.model.entity.ClassModel;
import org.unidal.mixin.model.entity.FieldModel;
import org.unidal.mixin.model.entity.InnerClassModel;
import org.unidal.mixin.model.entity.MethodModel;
import org.unidal.mixin.model.entity.MixinModel;
import org.unidal.mixin.model.entity.SourceModel;
import org.unidal.mixin.model.entity.TargetModel;

public interface IParser<T> {
   public MixinModel parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForClassModel(IMaker<T> maker, ILinker linker, ClassModel parent, T node);

   public void parseForFieldModel(IMaker<T> maker, ILinker linker, FieldModel parent, T node);

   public void parseForInnerClassModel(IMaker<T> maker, ILinker linker, InnerClassModel parent, T node);

   public void parseForMethodModel(IMaker<T> maker, ILinker linker, MethodModel parent, T node);

   public void parseForSourceModel(IMaker<T> maker, ILinker linker, SourceModel parent, T node);

   public void parseForTargetModel(IMaker<T> maker, ILinker linker, TargetModel parent, T node);
}

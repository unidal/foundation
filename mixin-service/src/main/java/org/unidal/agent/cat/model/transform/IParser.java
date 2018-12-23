/* THIS FILE WAS AUTO GENERATED BY codegen-maven-plugin, DO NOT EDIT IT */
package org.unidal.agent.cat.model.transform;

import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.EventModel;
import org.unidal.agent.cat.model.entity.MethodModel;
import org.unidal.agent.cat.model.entity.RootModel;
import org.unidal.agent.cat.model.entity.TransactionModel;

public interface IParser<T> {
   public RootModel parse(IMaker<T> maker, ILinker linker, T node);

   public void parseForClassModel(IMaker<T> maker, ILinker linker, ClassModel parent, T node);

   public void parseForEventModel(IMaker<T> maker, ILinker linker, EventModel parent, T node);

   public void parseForMethodModel(IMaker<T> maker, ILinker linker, MethodModel parent, T node);

   public void parseForTransactionModel(IMaker<T> maker, ILinker linker, TransactionModel parent, T node);
}
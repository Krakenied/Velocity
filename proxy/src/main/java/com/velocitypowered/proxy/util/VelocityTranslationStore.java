/*
 * Copyright (C) 2025 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.TranslationStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

public final class VelocityTranslationStore implements TranslationStore.StringBased<MessageFormat> {

  private final TranslationStore.StringBased<MessageFormat> backingStore;

  public VelocityTranslationStore(final TranslationStore.StringBased<MessageFormat> backingStore) {
    this.backingStore = backingStore;
  }

  @Override
  public void registerAll(final @NotNull Locale locale, final @NotNull Path path, final boolean escapeSingleQuotes) {
    this.backingStore.registerAll(locale, path, escapeSingleQuotes);
  }

  @Override
  public void registerAll(final @NotNull Locale locale, final @NotNull ResourceBundle bundle, final boolean escapeSingleQuotes) {
    this.backingStore.registerAll(locale, bundle, escapeSingleQuotes);
  }

  @Override
  public boolean contains(final @NotNull String key) {
    return this.backingStore.contains(key);
  }

  @Override
  public boolean contains(final @NotNull String key, final @NotNull Locale locale) {
    return this.backingStore.contains(key, locale);
  }

  @Override
  public void defaultLocale(final @NotNull Locale locale) {
    this.backingStore.defaultLocale(locale);
  }

  @Override
  public void register(final @NotNull String key, final @NotNull Locale locale, final MessageFormat translation) {
    this.backingStore.register(key, locale, translation);
  }

  @Override
  public void registerAll(final @NotNull Locale locale, final @NotNull Map<String, MessageFormat> translations) {
    this.backingStore.registerAll(locale, translations);
  }

  @Override
  public void registerAll(final @NotNull Locale locale, final @NotNull Set<String> keys, final Function<String, MessageFormat> function) {
    this.backingStore.registerAll(locale, keys, function);
  }

  @Override
  public void unregister(final @NotNull String key) {
    this.backingStore.unregister(key);
  }

  @Override
  public @NotNull Key name() {
    return this.backingStore.name();
  }

  @Override
  public @Nullable MessageFormat translate(final @NotNull String key, final @NotNull Locale locale) {
    return null;
  }

  @Override
  public @Nullable Component translate(final @NotNull TranslatableComponent component, final @NotNull Locale locale) {
    final MessageFormat translationFormat = this.backingStore.translate(component.key(), locale);

    if (translationFormat == null) {
      return null;
    }

    final String miniMessageString = translationFormat.toPattern();
    final Component resultingComponent = component.arguments().isEmpty()
        ? MiniMessage.miniMessage().deserialize(miniMessageString)
        : MiniMessage.miniMessage().deserialize(miniMessageString, new ArgumentTag(component.arguments()));

    return component.children().isEmpty()
        ? resultingComponent
        : resultingComponent.children(component.children());
  }

  @SuppressWarnings("ClassCanBeRecord")
  private static final class ArgumentTag implements TagResolver {

    private static final String ARGUMENT_NAME = "argument";
    private static final String ARGUMENT_NAME_SHORT = "arg";

    private final List<? extends ComponentLike> argumentComponents;

    public ArgumentTag(final @NotNull List<? extends ComponentLike> argumentComponents) {
      this.argumentComponents = Objects.requireNonNull(argumentComponents, "argumentComponents");
    }

    @Override
    public @NotNull Tag resolve(final @NotNull String name, final @NotNull ArgumentQueue arguments, final @NotNull Context ctx) throws ParsingException {
      final int index = arguments.popOr("No argument number provided")
          .asInt()
          .orElseThrow(() -> ctx.newException("Invalid argument number", arguments));

      if (index < 0 || index >= this.argumentComponents.size()) {
        throw ctx.newException("Invalid argument number", arguments);
      }

      return Tag.inserting(this.argumentComponents.get(index));
    }

    @Override
    public boolean has(final @NotNull String name) {
      return name.equals(ARGUMENT_NAME) || name.equals(ARGUMENT_NAME_SHORT);
    }
  }
}

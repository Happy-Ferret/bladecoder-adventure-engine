/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.bladecoder.engine.actions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.actions.Param.Type;
import com.bladecoder.engine.anim.Tween;
import com.bladecoder.engine.model.CharacterActor;
import com.bladecoder.engine.model.InteractiveActor;
import com.bladecoder.engine.model.Text;
import com.bladecoder.engine.model.TextManager;
import com.bladecoder.engine.model.World;

@ActionDescription("Says a text")
public class SayAction extends BaseCallbackAction {
	@ActionPropertyDescription("The target actor")
	@ActionProperty(type = Type.ACTOR)
	private String actor;

	@ActionPropertyDescription("The 'text' to show")
	@ActionProperty(type = Type.SMALL_TEXT)
	private String text;

	@ActionPropertyDescription("The 'soundId' to play if selected")
	@ActionProperty(type = Type.SOUND)
	private String soundId;

	@ActionProperty(required = true, defaultValue = "RECTANGLE")
	@ActionPropertyDescription("The type of the text.")

	private Text.Type type = Text.Type.RECTANGLE;

	@ActionProperty(defaultValue = "false")
	@ActionPropertyDescription("Queue the text if other text is showing, or show it immediately.")

	private boolean queue = false;

	private String previousAnim = null;

	@Override
	public boolean run(ActionCallback cb) {
		float x, y;
		Color color = null;

		setVerbCb(cb);
		InteractiveActor a = (InteractiveActor)World.getInstance().getCurrentScene().getActor(actor, false);

		if (soundId != null)
			a.playSound(soundId);

		if (text != null) {
			if (type != Text.Type.TALK) {
				x = y = TextManager.POS_SUBTITLE;
			} else {
				x = a.getX();
				y = a.getY() + a.getBBox().getBoundingRectangle().getHeight();
				
				color = ((CharacterActor)a).getTextColor();
				
				restoreStandPose((CharacterActor)a);
				startTalkAnim((CharacterActor)a);
			}

			World.getInstance().getTextManager().addText(text, x, y, queue, type, color, null,
					getWait()?this:null);
		}

		return getWait();
	}

	@Override
	public void resume() {
		if (type == Text.Type.TALK) {
			CharacterActor a = (CharacterActor) World.getInstance().getCurrentScene().getActor(actor, false);
			a.startAnimation(previousAnim, Tween.Type.SPRITE_DEFINED, 0, null);
		}

		super.resume();
	}

	private void restoreStandPose(CharacterActor a) {
		if (a == null)
			return;

		String fa = a.getRenderer().getCurrentAnimationId();

		// If the actor was already talking we restore the actor to the 'stand'
		// pose
		if (fa.startsWith(a.getTalkAnim())) {
			a.stand();
		}
	}

	private void startTalkAnim(CharacterActor a) {
		previousAnim = a.getRenderer().getCurrentAnimationId();

		a.talk();
	}

	@Override
	public void write(Json json) {
		json.writeValue("previousAnim", previousAnim);
		super.write(json);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		previousAnim = json.readValue("previousAnim", String.class, jsonData);
		super.read(json, jsonData);
	}

}

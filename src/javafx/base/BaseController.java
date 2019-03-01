package javafx.base;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class BaseController {

	protected Stage parentStage = null;
	protected Scene parentScene = null;

	public Stage getParentStage() {
		return parentStage;
	}

	public void setParentStage(Stage parentStage) {
		this.parentStage = parentStage;
	}

	public Scene getParentScene() {
		return parentScene;
	}

	public void setParentScene(Scene parentScene) {
		this.parentScene = parentScene;
	}


	public void init() {
	}

}

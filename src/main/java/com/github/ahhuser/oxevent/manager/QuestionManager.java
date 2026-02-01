package com.github.ahhuser.oxevent.manager;

import com.github.ahhuser.oxevent.OXEventPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QuestionManager {

    private final OXEventPlugin plugin;
    private final File questionsFile;
    private FileConfiguration questionsConfig;
    private final Map<Integer, Question> questions = new HashMap<>();

    public QuestionManager(OXEventPlugin plugin) {
        this.plugin = plugin;
        this.questionsFile = new File(plugin.getDataFolder(), "questions.yml");
        loadQuestions();
    }

    public void createQuestion(int id, boolean answer, String text) {
        Question q = new Question(id, answer, text);
        questions.put(id, q);
        saveQuestion(q);
    }

    public Question getQuestion(int id) {
        return questions.get(id);
    }

    private void loadQuestions() {
        if (!questionsFile.exists())
            return;
        questionsConfig = YamlConfiguration.loadConfiguration(questionsFile);

        for (String key : questionsConfig.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                boolean answer = questionsConfig.getBoolean(key + ".answer");
                String text = questionsConfig.getString(key + ".text");
                questions.put(id, new Question(id, answer, text));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void saveQuestion(Question q) {
        if (questionsConfig == null)
            questionsConfig = new YamlConfiguration();
        questionsConfig.set(q.id + ".answer", q.answer);
        questionsConfig.set(q.id + ".text", q.text);
        saveConfig();
    }

    private void saveConfig() {
        try {
            questionsConfig.save(questionsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public record Question(int id, boolean answer, String text) {
    }
}

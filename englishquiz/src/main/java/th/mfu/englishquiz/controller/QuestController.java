package th.mfu.englishquiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import th.mfu.englishquiz.entity.Quiz;
import th.mfu.englishquiz.entity.Quest;
import th.mfu.englishquiz.repository.QuestRepository;
import th.mfu.englishquiz.repository.QuizRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;          
import org.slf4j.LoggerFactory;  

@RestController
@RequestMapping("/api")  // Added base path for clarity
@CrossOrigin(origins = "*")  // Added to allow frontend connections
public class QuestController {

    private static final Logger logger = LoggerFactory.getLogger(QuestController.class); 

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private QuizRepository quizRepository;

    // GET all quests
    @GetMapping("/quests")
    public ResponseEntity<Collection<Quest>> listQuest() {
        List<Quest> quests = questRepository.findAll();
        logger.info("Fetched {} quests", quests.size()); 
        return new ResponseEntity<>(quests, HttpStatus.OK);
    }

    // NEW: Get quest by ID
    @GetMapping("/quests/{id}")
    public ResponseEntity<Quest> getQuestById(@PathVariable Long id) { 
        Optional<Quest> questOp = questRepository.findById(id);
        if (questOp.isPresent()) {
            logger.info("Quest found: {}", questOp.get().getQuestName()); // 
            return new ResponseEntity<>(questOp.get(), HttpStatus.OK);
        } else {
            logger.warn("Quest not found with ID {}", id); // 
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Create a new quest
    @PostMapping("/quests")
    public ResponseEntity<String> createQuest(@RequestBody Quest newQuest) {
        if (newQuest.getQuestName() == null || newQuest.getQuestName().trim().isEmpty()) { 
            return new ResponseEntity<>("Quest name cannot be empty", HttpStatus.BAD_REQUEST);
        }
        questRepository.save(newQuest);
        logger.info("New quest created: {}", newQuest.getQuestName()); // 
        return new ResponseEntity<>("Quest created", HttpStatus.CREATED);
    }

    // Update existing quest
    @PutMapping("/quests/{id}")
    public ResponseEntity<String> updateQuest(@PathVariable Long id, @RequestBody Quest updatedQuest) {
        Optional<Quest> questOp = questRepository.findById(id);
        if (questOp.isPresent()) {
            Quest quest = questOp.get();
            quest.setQuestName(updatedQuest.getQuestName());
            quest.setDescription(updatedQuest.getDescription());
            questRepository.save(quest);
            logger.info("Quest updated: {}", quest.getQuestName()); 
            return new ResponseEntity<>("Quest updated", HttpStatus.OK);
        } else {
            logger.warn("Attempted to update non-existent quest ID: {}", id); 
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete a quest
    @DeleteMapping("/quests/{id}")
    public ResponseEntity<String> deleteQuest(@PathVariable Long id) {
        if (!questRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent quest ID: {}", id); 
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        questRepository.deleteById(id);
        logger.info("Quest deleted with ID: {}", id); 
        return new ResponseEntity<>("Quest deleted successfully", HttpStatus.NO_CONTENT); 
    }

    // Get all quizzes under a specific quest
    @GetMapping("/quests/{questId}/quiz")
    public ResponseEntity<Collection<Quiz>> listQuiz(@PathVariable Long questId) {
        Optional<Quest> questOp = questRepository.findById(questId);
        if (questOp.isEmpty()) {
            logger.warn("Quest not found for quiz listing: ID {}", questId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Collection<Quiz> quizzes = quizRepository.findByQuestId(questId);
        logger.info("Fetched {} quizzes for quest ID {}", quizzes.size(), questId); 
        return new ResponseEntity<>(quizzes, HttpStatus.OK);
    }

    // Create a new quiz under a specific quest
    // Create a new quiz under a specific quest
@PostMapping("/quests/{questId}/quiz")
public ResponseEntity<String> createQuiz(@RequestBody Quiz newQuiz, @PathVariable Long questId) { 
    Optional<Quest> questOp = questRepository.findById(questId);
    if (questOp.isEmpty()) {
        logger.warn("Quest not found for adding quiz: ID {}", questId); 
        return new ResponseEntity<>("Quest not found", HttpStatus.NOT_FOUND);
    }

    Quest quest = questOp.get();
    newQuiz.setQuest(quest);
    quizRepository.save(newQuiz);
    logger.info("New quiz added to quest ID {}: {}", questId, newQuiz.getTitle()); 
    return new ResponseEntity<>("Quiz saved successfully", HttpStatus.CREATED);
}


    // Added: Global exception handler (optional, for debugging)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("An error occurred: {}", e.getMessage());
        return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}


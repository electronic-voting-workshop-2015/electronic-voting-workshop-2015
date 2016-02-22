$N = 7 #number of parties
$T = 5 #threshold

class CreateTables < ActiveRecord::Migration
  def up
    create_table :ZKP do |t|
      t.integer  :vote_id
      N.times do |i| # N = number of parties
        t.string  :"party_#{i+1}"
      end
    end #ZKP
    
    create_table :votes do |t|
      #t.integer   :vote_id                         ID column generated automatically
      t.string    :vote_value, null: false
      t.integer   :ballot_box, null: false
      t.datetime  :time                             #trigger will populate this field
      t.integer   :serial_number, null: false
      t.integer   :race_id, null: false
      
      execute "CREATE TRIGGER `votes_PREVENT_DEL` BEFORE DELETE ON `votes` FOR EACH ROW
                begin signal sqlstate '45000' set message_text = 'Deleting values from this table is not allowed';"
      
      execute "CREATE TRIGGER `votes_UPDATE_TIME` AFTER INSERT ON `votes` FOR EACH ROW
                            begin SET NEW.time = SYSDATE();"   

    end #votes
    
    create_table :commitments do |t|
      t.integer   :party_id
      T.times do |i|                                      # T = threshold
        t.string  :"threshold_#{i+1}"
      end
      
      N.times do |i|
        execute "insert into commitments(party_id) values(#{i+1})" #create a row for each party
      end
    end #commitments
    
    create_table :SendCommitments do |t|
      t.integer   :party_id
      
      N.times do |i|
        t.string  :"party_#{i+1}"
      end
      
      N.times do |i|
        execute "insert into SendCommitments(party_id) values(#{i+1})" #create a row for each party
      end
    end #SendCommitments
    
    create_table :logs do |t|
     # t.integer   :log_id                          ID column generated automatically
      t.string    :log_value, null: false
      t.datetime  :time                             #trigger will populate this field
      t.string    :source, null: false
      
      execute "CREATE TRIGGER `logs_PREVENT_DEL` BEFORE DELETE ON `logs` FOR EACH ROW
                      begin signal sqlstate '45000' set message_text = 'Deleting values from this table is not allowed';"
      
      execute "CREATE TRIGGER `logs_UPDATE_TIME` AFTER INSERT ON `logs` FOR EACH ROW
                            begin SET NEW.time = SYSDATE();"  
    end #logs
    
  end #up

end #CreateTables


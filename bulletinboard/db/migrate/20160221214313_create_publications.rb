class CreatePublications < ActiveRecord::Migration
  def change
      create_table :publications do |t|
          t.binary :content
      end
  end
end

class CreateTables < ActiveRecord::Migration
  def up
    create_table :ZKP do |t|
      t.integer  :vote_id
      N.times do |i| # N = number of parties
        t.string  :"party_#{i+1}"
      end
    end #ZKP
    
    create_table :votes do |t|
      t.integer   :vote_id
      t.string    :vote_value
      t.integer   :ballot_box
      t.datetime  :date
      t.integer   :serial_number
      t.integer   :race_id
    end #votes
    
    create_table :commitments do |t|
      t.integer   :party_id
      T.times do |i| # T = threshold
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
      t.integer   :log_id
      t.string    :log_value
      t.datetime  :time
      t.string    :source
    end #logs
    
  end #up

end #CreateTables


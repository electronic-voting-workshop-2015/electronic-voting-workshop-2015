class CreateVotes < ActiveRecord::Migration
	def up
		create_table :votes do |t|
		  #t.integer   :vote_id                         ID column generated automatically
		  t.text    :vote_value, null: false
		  t.integer   :ballot_box, null: false
		  t.integer   :serial_number, null: false
		  t.integer   :race_id, null: false

    	end #votes

				  execute "CREATE TRIGGER `votes_PREVENT_DEL` BEFORE DELETE ON `votes` FOR EACH ROW begin signal sqlstate '45000' set message_text = 'Deleting values from this table is not allowed'; end"
	end #up

	def down
		drop_table  :votes
	end
end

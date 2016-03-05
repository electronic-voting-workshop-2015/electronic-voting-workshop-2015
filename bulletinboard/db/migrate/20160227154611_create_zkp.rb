class CreateZkp < ActiveRecord::Migration
  	def up
		create_table :ZKP do |t|
		  t.integer  :vote_id
		  t.integer  :party_id
		  t.integer :race_id
		  t.text :zkp
    	end #ZKP
	end #up

	def down
		drop_table  :ZKP
	end #down
end

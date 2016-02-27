class CreateZkp < ActiveRecord::Migration
  	def up
		create_table :ZKP do |t|
		  t.integer  :vote_id
		  7.times do |i| # N = number of parties
		    t.text  :"party_#{i+1}"
		  end
    	end #ZKP
	end #up

	def down
		drop_table  :ZKP
	end #down
end

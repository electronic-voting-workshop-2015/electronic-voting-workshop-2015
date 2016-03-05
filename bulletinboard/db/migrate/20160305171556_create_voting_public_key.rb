class CreateVotingPublicKey < ActiveRecord::Migration
  def change
		create_table :voting_public_keys, id: false do |t|
    	    t.binary :content
	    end
  end
end
